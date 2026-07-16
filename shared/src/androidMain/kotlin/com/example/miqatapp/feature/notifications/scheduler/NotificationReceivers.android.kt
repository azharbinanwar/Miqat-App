package com.example.miqatapp.feature.notifications.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.miqatapp.core.platform.AppCtx

// Silent nightly wake -> roll the window forward without the app being opened.
class NotificationRebuildReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        NotificationScheduler.rebuildAsync()
    }
}

// Alarms die on reboot; rebuild.
class NotificationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        NotificationScheduler.rebuildAsync()
    }
}

// Clock or timezone changed; armed alarms target stale instants.
class NotificationTimeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        NotificationScheduler.rebuildAsync()
    }
}
