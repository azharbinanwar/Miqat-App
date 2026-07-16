package com.example.miqatapp.feature.notifications.scheduler

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.miqatapp.core.platform.AppCtx
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.notification_channel_dhikr
import com.example.miqatapp.resources.notification_channel_group
import com.example.miqatapp.resources.notification_channel_nafil
import com.example.miqatapp.resources.notification_channel_prayer
import com.example.miqatapp.resources.notification_channel_quran
import com.example.miqatapp.resources.notification_channel_verse
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

// Fires at an alert's time and posts it. Title/body were resolved at schedule time and ride the intent;
// the channel is picked from the target so users can tune each category in system settings.
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
        registerChannels(ctx)
        val channel = channelFor(key.substringBefore(':')) // target is the eventKey's first segment
        val iconId = ctx.resources.getIdentifier("ic_notification", "drawable", ctx.packageName)
        val notification = NotificationCompat.Builder(ctx, channel.id)
            .setContentTitle(title)
            .apply { if (body.isNotEmpty()) setContentText(body) }
            .setSmallIcon(if (iconId != 0) iconId else android.R.drawable.ic_popup_reminder)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) nm.notify(key.hashCode(), notification) // unique per event
    }

    // One channel per category, all under a single group, so each is tunable in system settings.
    private enum class Channel(val id: String, val nameRes: StringResource, val importance: Int) {
        PRAYER("prayer", Res.string.notification_channel_prayer, NotificationManager.IMPORTANCE_HIGH),
        QURAN("quran", Res.string.notification_channel_quran, NotificationManager.IMPORTANCE_DEFAULT),
        DHIKR("dhikr", Res.string.notification_channel_dhikr, NotificationManager.IMPORTANCE_DEFAULT),
        NAFIL("nafil", Res.string.notification_channel_nafil, NotificationManager.IMPORTANCE_DEFAULT),
        VERSE("verse", Res.string.notification_channel_verse, NotificationManager.IMPORTANCE_DEFAULT),
    }

    private fun channelFor(target: String): Channel = when (target) {
        NotificationTarget.MULK, NotificationTarget.KAHF -> Channel.QURAN
        NotificationTarget.MORNING, NotificationTarget.EVENING -> Channel.DHIKR
        NotificationTarget.TAHAJJUD, NotificationTarget.ISHRAQ -> Channel.NAFIL
        "verse", "hadith" -> Channel.VERSE
        else -> Channel.PRAYER // fajr..isha, jumuah, test — anything unmapped still posts
    }

    companion object {
        private const val GROUP = "reminders"
        private val RETIRED_CHANNELS = listOf("prayer_reminders", "surah") // old channel ids, deleted on run
        const val EXTRA_KEY = "key"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"

        // Create the group + every channel together (lazily, on the first notification that fires),
        // so all categories show up in system settings at once. Idempotent.
        fun registerChannels(ctx: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            RETIRED_CHANNELS.forEach { nm.deleteNotificationChannel(it) }
            nm.createNotificationChannelGroup(NotificationChannelGroup(GROUP, runBlocking { getString(Res.string.notification_channel_group) }))
            Channel.entries.forEach { c ->
                nm.createNotificationChannel(NotificationChannel(c.id, runBlocking { getString(c.nameRes) }, c.importance).apply { group = GROUP })
            }
        }

        fun intent(ctx: Context, e: NotificationEvent, title: String, body: String): Intent =
            Intent(ctx, NotificationReceiver::class.java)
                .putExtra(EXTRA_KEY, e.eventKey)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_BODY, body)
    }
}
