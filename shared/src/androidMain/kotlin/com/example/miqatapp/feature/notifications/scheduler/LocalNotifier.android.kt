package com.example.miqatapp.feature.notifications.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.miqatapp.core.constants.defaults.NotificationDefaults
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.platform.AppCtx
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

actual object LocalNotifier {
    private const val BASE = 200        // request-code base for the 0..63 slots (clear of Focus's codes)
    private const val SLOTS = 64
    private const val DAILY_CODE = 300  // the silent nightly rebuild alarm
    private val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    actual fun schedule(event: NotificationEvent, title: String, body: String) {
        val ctx = AppCtx.context
        android.util.Log.i("MiqatNotif", "armed slot ${event.slotId} ${event.eventKey} in ${(event.fireAtMillis - System.currentTimeMillis()) / 1000}s") // dev
        setAlarmClockCompat(ctx, am(ctx), event.fireAtMillis, PendingIntent.getBroadcast(ctx, BASE + event.slotId, NotificationReceiver.intent(ctx, event, title, body), FLAGS))
    }

    actual fun cancelAll() {
        val ctx = AppCtx.context
        val am = am(ctx)
        for (i in 0 until SLOTS) am.cancel(PendingIntent.getBroadcast(ctx, BASE + i, Intent(ctx, NotificationReceiver::class.java), FLAGS))
        armDaily(ctx, am)
    }

    // Silent ~00:05 wake to roll the window forward with no app open. No icon, no notification.
    private fun armDaily(ctx: Context, am: AlarmManager) {
        val tz = TimeZone.currentSystemDefault()
        val at = LocalDateTime(currentDate().plus(1, DateTimeUnit.DAY), LocalTime(NotificationDefaults.Scheduler.dailyRebuildHour, NotificationDefaults.Scheduler.dailyRebuildMinute)).toInstant(tz).toEpochMilliseconds()
        setIdle(am, at, PendingIntent.getBroadcast(ctx, DAILY_CODE, Intent(ctx, NotificationRebuildReceiver::class.java), FLAGS))
    }

    // Alerts: setAlarmClock is NOT Doze-throttled, so it fires exactly even backgrounded/killed (same as Prayer Focus).
    private fun setAlarmClockCompat(ctx: Context, am: AlarmManager, at: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi) // no exact-alarm permission: inexact fallback
        } else {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, launchPi(ctx)), pi)
        }
    }

    // Daily rebuild only: silent, no icon; once a day so Doze throttling is irrelevant.
    private fun setIdle(am: AlarmManager, at: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        }
    }

    private fun launchPi(ctx: Context): PendingIntent {
        val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName) ?: Intent()
        return PendingIntent.getActivity(ctx, 9100, launch, FLAGS)
    }

    private fun am(ctx: Context) = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
