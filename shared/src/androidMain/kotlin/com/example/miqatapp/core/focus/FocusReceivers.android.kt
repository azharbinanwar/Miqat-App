package com.example.miqatapp.core.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.miqatapp.core.platform.AppCtx

// Fires ~20s before a slot: starts the service, which mutes at the slot and restores at the end.
class FocusAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        val start = intent.getLongExtra(PhoneSilenceService.EXTRA_START, 0L)
        val end = intent.getLongExtra(PhoneSilenceService.EXTRA_END, 0L)
        val label = intent.getStringExtra(PhoneSilenceService.EXTRA_LABEL) ?: "prayer"
        val mode = intent.getStringExtra(PhoneSilenceService.EXTRA_MODE) ?: SilenceMode.Vibrate.name
        android.util.Log.i("MiqatFocus", "alarm fired ($label/$mode), slot in ${(start - System.currentTimeMillis()) / 1000}s")
        PhoneSilencer.silence(start, end, label, mode)
    }
}

// The notification's action buttons.
class FocusActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        when (intent.action) {
            ACTION_UNMUTE -> PhoneSilencer.unmuteNow()
            ACTION_EXTEND -> PhoneSilencer.extend()
            ACTION_MODE -> PhoneSilencer.toggleMode()
        }
    }

    companion object {
        const val ACTION_UNMUTE = "com.example.miqatapp.focus.UNMUTE"
        const val ACTION_EXTEND = "com.example.miqatapp.focus.EXTEND"
        const val ACTION_MODE = "com.example.miqatapp.focus.MODE"
    }
}

// Fires ~00:10 nightly to roll the prayer windows forward a day.
class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        PhoneSilencer.rescheduleAll()
    }
}

// Alarms don't survive a reboot; re-arm everything on boot.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        PhoneSilencer.rescheduleAll()
    }
}
