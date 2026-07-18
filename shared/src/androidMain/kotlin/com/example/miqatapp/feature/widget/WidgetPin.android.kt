package com.example.miqatapp.feature.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import com.example.miqatapp.core.platform.AppCtx

actual fun pinPrayerCardWidget() {
    val ctx = AppCtx.context
    val mgr = ctx.getSystemService(AppWidgetManager::class.java) ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mgr.isRequestPinAppWidgetSupported) {
        mgr.requestPinAppWidget(ComponentName(ctx, PrayerCardWidgetReceiver::class.java), null, null)
    }
}
