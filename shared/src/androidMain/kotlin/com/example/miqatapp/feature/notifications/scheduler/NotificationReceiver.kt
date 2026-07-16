package com.example.miqatapp.feature.notifications.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.miqatapp.core.platform.AppCtx
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString

// Fires at an alert's time and posts it.
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        val target = intent.getStringExtra(EXTRA_TARGET) ?: return
        val kind = intent.getStringExtra(EXTRA_KIND) ?: return
        val key = intent.getStringExtra(EXTRA_KEY) ?: "$target:$kind"
        val vibrate = intent.getBooleanExtra(EXTRA_VIBRATE, true)
        val title = if (target == "test") "Test #${key.substringAfterLast(':')}" // distinct per test
        else runBlocking { getString(labelRes(target)) } // bundled string, resolves fast
        android.util.Log.i("MiqatNotif", "fired $key") // dev: watch in Logcat
        post(context, key, title, vibrate)
    }

    private fun post(ctx: Context, key: String, title: String, vibrate: Boolean) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Prayer reminders", NotificationManager.IMPORTANCE_HIGH).also { it.enableVibration(vibrate) })
        }
        val iconId = ctx.resources.getIdentifier("ic_notification", "drawable", ctx.packageName)
        val notif = NotificationCompat.Builder(ctx, CHANNEL)
            .setContentTitle(title)
            .setSmallIcon(if (iconId != 0) iconId else android.R.drawable.ic_popup_reminder)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) nm.notify(key.hashCode(), notif) // unique per event
    }

    companion object {
        private const val CHANNEL = "prayer_reminders"
        const val EXTRA_TARGET = "target"
        const val EXTRA_KIND = "kind"
        const val EXTRA_KEY = "key"
        const val EXTRA_VIBRATE = "vibrate"
        const val EXTRA_SOUND = "sound"

        fun intent(ctx: Context, e: NotificationEvent): Intent = Intent(ctx, NotificationReceiver::class.java)
            .putExtra(EXTRA_TARGET, e.target)
            .putExtra(EXTRA_KIND, e.kind.name)
            .putExtra(EXTRA_KEY, e.eventKey)
            .putExtra(EXTRA_VIBRATE, e.vibrate)
            .putExtra(EXTRA_SOUND, e.sound)
    }
}
