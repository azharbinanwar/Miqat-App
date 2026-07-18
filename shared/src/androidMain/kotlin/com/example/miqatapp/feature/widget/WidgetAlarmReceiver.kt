package com.example.miqatapp.feature.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.miqatapp.core.platform.AppCtx

// Fires at each prayer transition (and every ~15 min) to redraw the widget with a fresh next/countdown.
// refresh() recomputes for the current day, so it also handles the midnight rollover, then re-arms itself.
class WidgetAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AppCtx.context = context.applicationContext
        WidgetPublisher.refresh()
    }
}
