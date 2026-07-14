package com.example.miqatapp.core.focus

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.platform.AppCtx
import com.example.miqatapp.core.prefs.PrefsService
import com.example.miqatapp.core.store.FocusTestStore
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

actual object PhoneSilencer {
    private const val LEAD = 20_000L // start the service ~20s before the slot so it's up in time to mute
    private const val REAL_MAX = 32  // cancel range for the real per-prayer alarms (codes 0 until this)
    private const val DAILY_CODE = 40 // outside REAL_MAX: the once-a-day re-arm alarm
    private const val END_CODE = 41   // the "double alarm": fires at the active window's end to guarantee restore
    private const val TEST_BASE = 100 // saved test-slot alarms use codes TEST_BASE until TEST_BASE + TEST_MAX
    private const val TEST_MAX = 64

    // Clear our alarms and re-arm: one per enabled prayer window (today's remaining + tomorrow) + saved test slots.
    actual fun rescheduleAll() {
        val ctx = AppCtx.context
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (code in 0 until REAL_MAX) am.cancel(alarmPi(ctx, code, 0, 0, "", ""))
        for (code in TEST_BASE until TEST_BASE + TEST_MAX) am.cancel(alarmPi(ctx, code, 0, 0, "", ""))
        FocusWindows.upcoming().take(REAL_MAX).forEachIndexed { i, w -> arm(ctx, am, i, w.startMillis, w.endMillis, w.label, w.mode.name) }
        FocusTestStore.prunePast(System.currentTimeMillis())
        FocusTestStore.slots.value.take(TEST_MAX).forEachIndexed { i, s -> arm(ctx, am, TEST_BASE + i, s.start, s.end, "prayer", s.mode) }
        armDaily(ctx, am) // roll windows forward even if the app is never opened
    }

    // Wakes ~00:10 each night and reschedules, so tomorrow's windows exist without the app being opened.
    private fun armDaily(ctx: Context, am: AlarmManager) {
        val tz = TimeZone.currentSystemDefault()
        val at = LocalDateTime(currentDate().plus(1, DateTimeUnit.DAY), LocalTime(0, 10)).toInstant(tz).toEpochMilliseconds()
        val pi = PendingIntent.getBroadcast(ctx, DAILY_CODE, Intent(ctx, RescheduleReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    private fun arm(ctx: Context, am: AlarmManager, code: Int, start: Long, end: Long, label: String, mode: String) {
        val at = maxOf(System.currentTimeMillis(), start - LEAD)
        val pi = alarmPi(ctx, code, start, end, label, mode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) // no exact-alarm permission: inexact is the only legal fallback
        } else {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, launchPi(ctx)), pi)
        }
    }

    // The safety net: a one-shot alarm at the window's end that wakes the app and runs restoreIfStuck().
    // Same request code + FLAG_UPDATE_CURRENT means re-arming (extend) replaces the previous alarm.
    internal fun armEndAlarm(endMillis: Long) {
        val ctx = AppCtx.context
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = endAlarmPi(ctx)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endMillis, pi)
        } else {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(endMillis, launchPi(ctx)), pi)
        }
    }

    internal fun cancelEndAlarm() {
        val ctx = AppCtx.context
        (ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(endAlarmPi(ctx))
    }

    private fun endAlarmPi(ctx: Context): PendingIntent = PendingIntent.getBroadcast(
        ctx, END_CODE,
        Intent(ctx, FocusActionReceiver::class.java).setAction(FocusActionReceiver.ACTION_RESTORE),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun alarmPi(ctx: Context, code: Int, start: Long, end: Long, label: String, mode: String): PendingIntent =
        PendingIntent.getBroadcast(
            ctx, code,
            Intent(ctx, FocusAlarmReceiver::class.java)
                .putExtra(PhoneSilenceService.EXTRA_START, start)
                .putExtra(PhoneSilenceService.EXTRA_END, end)
                .putExtra(PhoneSilenceService.EXTRA_LABEL, label)
                .putExtra(PhoneSilenceService.EXTRA_MODE, mode),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun launchPi(ctx: Context): PendingIntent {
        val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName) ?: Intent()
        return PendingIntent.getActivity(ctx, 9000, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    actual fun silence(startMillis: Long, endMillis: Long, label: String, mode: String) {
        val ctx = AppCtx.context
        val i = Intent(ctx, PhoneSilenceService::class.java)
            .putExtra(PhoneSilenceService.EXTRA_START, startMillis)
            .putExtra(PhoneSilenceService.EXTRA_END, endMillis)
            .putExtra(PhoneSilenceService.EXTRA_LABEL, label)
            .putExtra(PhoneSilenceService.EXTRA_MODE, mode)
        ContextCompat.startForegroundService(ctx, i)
    }

    actual fun silenceFor(durationMillis: Long) {
        val now = System.currentTimeMillis()
        silence(now, now + durationMillis, "prayer", SilenceMode.Vibrate.name)
    }

    actual fun restoreIfStuck() {
        if (!Ringer.hasSaved()) return // nothing muted by us (service finished cleanly) -> nothing to do
        val end = PrefsService.getStringOrNull(PrefConst.FOCUS_SILENCE_END)?.toLongOrNull() ?: 0L
        if (System.currentTimeMillis() >= end) {
            // Window over but the service died before restoring -> put the ringer back now and clean up fully.
            // Never (re)starts anything here, so nothing can loop after the window has passed.
            Ringer.restore()
            clearWindowPrefs()
            cancelEndAlarm()
            val ctx = AppCtx.context
            ctx.stopService(Intent(ctx, PhoneSilenceService::class.java))
        } else {
            silence(System.currentTimeMillis(), end, savedLabel(), savedMode()) // still mid-window -> restart to finish
        }
    }

    actual fun unmuteNow() {
        Ringer.restore(forceNormal = true)
        clearWindowPrefs()
        cancelEndAlarm()
        val ctx = AppCtx.context
        ctx.stopService(Intent(ctx, PhoneSilenceService::class.java))
    }

    internal fun clearWindowPrefs() {
        PrefsService.remove(PrefConst.FOCUS_SILENCE_END)
        PrefsService.remove(PrefConst.FOCUS_SILENCE_MODE)
        PrefsService.remove(PrefConst.FOCUS_SILENCE_LABEL)
    }

    // Both restart the running service with new params (silence() cancels the old job and starts a fresh one).
    actual fun extend() {
        val end = PrefsService.getStringOrNull(PrefConst.FOCUS_SILENCE_END)?.toLongOrNull() ?: return
        silence(System.currentTimeMillis(), end + 5 * 60_000L, savedLabel(), savedMode())
    }

    actual fun toggleMode() {
        val end = PrefsService.getStringOrNull(PrefConst.FOCUS_SILENCE_END)?.toLongOrNull() ?: return
        val next = if (savedMode() == SilenceMode.Silent.name) SilenceMode.Vibrate.name else SilenceMode.Silent.name
        silence(System.currentTimeMillis(), end, savedLabel(), next)
    }

    private fun savedLabel() = PrefsService.getStringOrNull(PrefConst.FOCUS_SILENCE_LABEL) ?: "prayer"
    private fun savedMode() = PrefsService.getStringOrNull(PrefConst.FOCUS_SILENCE_MODE) ?: SilenceMode.Vibrate.name
}
