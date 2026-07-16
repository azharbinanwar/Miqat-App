package com.example.miqatapp.core.focus

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.prefs.PrefsService
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.focus_extend
import com.example.miqatapp.resources.focus_prayed
import com.example.miqatapp.resources.focus_unmute
import com.example.miqatapp.resources.notif_focus_body
import com.example.miqatapp.resources.notif_focus_title
import com.example.miqatapp.resources.prayer_focus
import com.example.miqatapp.resources.prayer_jumuah
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.getString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Stays alive across swipe-from-recents, so it can mute at the window start and restore at the end
// in one process life (no second cold-start for the OEM to block). Force Stop still kills it.
class PhoneSilenceService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val start = intent?.getLongExtra(EXTRA_START, 0L) ?: 0L
        val end = intent?.getLongExtra(EXTRA_END, 0L) ?: 0L
        val label = intent?.getStringExtra(EXTRA_LABEL) ?: "prayer"
        val mode = intent?.getStringExtra(EXTRA_MODE) ?: SilenceMode.Vibrate.name
        log("service started ($label/$mode), mutes in ${(start - System.currentTimeMillis()) / 1000}s")
        PrefsService.putString(PrefConst.FOCUS_SILENCE_END, end.toString()) // so catch-up can heal a killed service
        PrefsService.putString(PrefConst.FOCUS_SILENCE_MODE, mode)
        PrefsService.putString(PrefConst.FOCUS_SILENCE_LABEL, label)
        // Double alarm: even if the OEM freezes this service, the OS wakes us at `end` and restores.
        // Extend/toggle restart the service through here, so the alarm is always re-armed to the latest end.
        PhoneSilencer.armEndAlarm(end)
        goForeground(start, end, label, mode)
        job?.cancel()
        job = scope.launch {
            delay((start - System.currentTimeMillis()).coerceAtLeast(0))
            log("mute -> ${if (Ringer.mute(mode)) "SILENT" else "VIBRATE"}")
            delay((end - System.currentTimeMillis()).coerceAtLeast(0))
            if (Ringer.restore()) log("restore ringer")
            PhoneSilencer.clearWindowPrefs()
            PhoneSilencer.cancelEndAlarm() // job done in-process; the safety net isn't needed anymore
            stopSelf()
        }
        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        job?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    private fun goForeground(start: Long, end: Long, label: String, mode: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Prayer Focus", NotificationManager.IMPORTANCE_LOW))
        }
        // Follow the app's chosen language (or system when not overridden); the UI's locale isn't applied in a cold-start service.
        PrefsService.getStringOrNull(PrefConst.LANGUAGE)?.let { java.util.Locale.setDefault(java.util.Locale(it)) }
        val pattern = SettingsStore.timeFormat.value.pattern
        fun action(a: String, code: Int) = PendingIntent.getBroadcast(
            this, code, Intent(this, FocusActionReceiver::class.java).setAction(a),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        var title = ""; var body = ""; var unmute = ""; var extend = ""; var prayed = ""
        runBlocking { // resolving a few bundled strings; fast enough for startForeground
            title = getString(Res.string.notif_focus_title, prayerName(label))
            body = getString(Res.string.notif_focus_body, fmt(end, pattern))
            unmute = getString(Res.string.focus_unmute)
            extend = getString(Res.string.focus_extend)
            prayed = getString(Res.string.focus_prayed)
        }
        val iconId = resources.getIdentifier("ic_notification", "drawable", packageName)
        // Button order = add order (left to right): +5 min | Prayed | Unmute — Unmute rightmost, under the thumb.
        val notif = NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(if (iconId != 0) iconId else android.R.drawable.ic_lock_silent_mode)
            .setOngoing(true)
            .addAction(0, extend, action(FocusActionReceiver.ACTION_EXTEND, 2))
            .addAction(0, prayed, action(FocusActionReceiver.ACTION_PRAYED, 4))
            .addAction(0, unmute, action(FocusActionReceiver.ACTION_UNMUTE, 1))
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }

    // Localized prayer name from the English label passed through the intent.
    private suspend fun prayerName(label: String): String = when (label) {
        "Jumu'ah" -> getString(Res.string.prayer_jumuah)
        "prayer" -> getString(Res.string.prayer_focus)
        else -> runCatching { getString(Miqat.valueOf(label).labelRes) }.getOrDefault(label)
    }

    private fun fmt(ms: Long, pattern: String): String {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = ms }
        return LocalTime(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE)).format(pattern)
    }

    private fun log(msg: String) = android.util.Log.i("MiqatFocus", "svc $msg")

    companion object {
        const val EXTRA_START = "start"
        const val EXTRA_END = "end"
        const val EXTRA_LABEL = "label"
        const val EXTRA_MODE = "mode"
        private const val CHANNEL = "prayer_focus"
        private const val NOTIF_ID = 4711
    }
}
