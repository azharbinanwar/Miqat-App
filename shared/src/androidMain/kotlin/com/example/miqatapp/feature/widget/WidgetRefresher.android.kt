package com.example.miqatapp.feature.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.setWidgetPreviews
import androidx.glance.appwidget.updateAll
import com.example.miqatapp.core.platform.AppCtx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

// Redraw the widget + re-arm the transition alarms off the freshly-written snapshot. Mirrors LocalNotifier's alarm pattern.
actual object WidgetRefresher {
    private const val BASE = 500        // request-code base for transition slots (clear of Focus/Notification codes)
    private const val SLOTS = 8         // at most 5 today + tomorrow-Fajr; 8 is headroom
    private const val PERIODIC = 520
    private val FLAGS = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    actual fun refresh() {
        val ctx = AppCtx.context
        scope.launch {
            repaint(ctx)
            pushPreviews()
        }
        armAlarms(ctx)
    }

    // Instant look change from the editor: repaint only, no alarm re-arm / preview push.
    actual fun redraw() {
        val ctx = AppCtx.context
        scope.launch { repaint(ctx) }
    }

    private suspend fun repaint(ctx: Context) {
        MinimalWidget().updateAll(ctx)
        PrayerCardWidget().updateAll(ctx)
        PrayerTimesWidget().updateAll(ctx)
        PrayerBarWidget().updateAll(ctx)
        PrayerNextWidget().updateAll(ctx)
        PrayerTileWidget().updateAll(ctx)
        PrayerIconWidget().updateAll(ctx)
    }

    // Android 15+ generated previews: render the real widgets in the picker (ring/logo/hijri and all).
    // Push ONCE ever — setWidgetPreviews is heavily rate-limited; re-pushing trips the throttle and the
    // picker falls back to the static previewLayout. Persist a flag so relaunches don't re-push.
    private suspend fun pushPreviews() {
        if (Build.VERSION.SDK_INT < 35) return
        val prefs = AppCtx.context.getSharedPreferences("miqat_widget", Context.MODE_PRIVATE)
        if (prefs.getBoolean("previews_pushed", false)) return
        val mgr = GlanceAppWidgetManager(AppCtx.context)
        val ok = runCatching {
            mgr.setWidgetPreviews<MinimalWidgetReceiver>()
            mgr.setWidgetPreviews<PrayerCardWidgetReceiver>()
            mgr.setWidgetPreviews<PrayerTimesWidgetReceiver>()
            mgr.setWidgetPreviews<PrayerBarWidgetReceiver>()
            mgr.setWidgetPreviews<PrayerNextWidgetReceiver>()
            mgr.setWidgetPreviews<PrayerTileWidgetReceiver>()
            mgr.setWidgetPreviews<PrayerIconWidgetReceiver>()
        }.isSuccess
        if (ok) prefs.edit().putBoolean("previews_pushed", true).apply()
    }

    private fun armAlarms(ctx: Context) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until SLOTS) am.cancel(pi(ctx, BASE + i))
        val snap = WidgetStore.read()?.let { runCatching { Json.decodeFromString<WidgetSnapshot>(it) }.getOrNull() }
        val now = System.currentTimeMillis()
        // Redraw exactly when the "next" prayer flips (each future prayer + tomorrow's Fajr).
        (snap?.let { it.prayers.map(WidgetPrayer::atMillis) + it.nextFajr.atMillis } ?: emptyList())
            .filter { it > now }.sorted().take(SLOTS)
            .forEachIndexed { i, at -> setAlarmClockCompat(ctx, am, at, pi(ctx, BASE + i)) }
        // Coarse countdown freshness between transitions — inexact, Doze-batched.
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, now + FIFTEEN_MIN, FIFTEEN_MIN, pi(ctx, PERIODIC))
    }

    private fun pi(ctx: Context, code: Int) =
        PendingIntent.getBroadcast(ctx, code, Intent(ctx, WidgetAlarmReceiver::class.java), FLAGS)

    // setAlarmClock is NOT Doze-throttled, so the flip lands on time even backgrounded (same as the notifier).
    private fun setAlarmClockCompat(ctx: Context, am: AlarmManager, at: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi)
        } else {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(at, launchPi(ctx)), pi)
        }
    }

    private fun launchPi(ctx: Context): PendingIntent {
        val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName) ?: Intent()
        return PendingIntent.getActivity(ctx, 9200, launch, FLAGS)
    }

    private const val FIFTEEN_MIN = 15L * 60 * 1000
}
