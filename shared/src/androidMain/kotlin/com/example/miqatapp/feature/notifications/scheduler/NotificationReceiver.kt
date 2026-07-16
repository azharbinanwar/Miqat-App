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

// Fires at an alert's time and posts it. Title/body were resolved at schedule time and ride the intent.
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        val key = intent.getStringExtra(EXTRA_KEY) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        android.util.Log.i("MiqatNotif", "fired $key") // dev: watch in Logcat
        post(context, key, title, body)
    }

    private fun post(ctx: Context, key: String, title: String, body: String) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(CHANNEL, "Prayer reminders", NotificationManager.IMPORTANCE_HIGH))
        }
        val iconId = ctx.resources.getIdentifier("ic_notification", "drawable", ctx.packageName)
        val notif = NotificationCompat.Builder(ctx, CHANNEL)
            .setContentTitle(title)
            .apply { if (body.isNotEmpty()) setContentText(body) }
            .setSmallIcon(if (iconId != 0) iconId else android.R.drawable.ic_popup_reminder)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) nm.notify(key.hashCode(), notif) // unique per event
    }

    companion object {
        private const val CHANNEL = "prayer_reminders"
        const val EXTRA_KEY = "key"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"

        fun intent(ctx: Context, e: NotificationEvent, title: String, body: String): Intent =
            Intent(ctx, NotificationReceiver::class.java)
                .putExtra(EXTRA_KEY, e.eventKey)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_BODY, body)
    }
}
