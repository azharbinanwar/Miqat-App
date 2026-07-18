package com.example.miqatapp.feature.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Build
import com.example.miqatapp.core.platform.AppCtx

actual fun pinWidget(kind: WidgetKind) {
    val receiver = when (kind) {
        WidgetKind.Times -> PrayerTimesWidgetReceiver::class.java
        WidgetKind.Bar -> PrayerBarWidgetReceiver::class.java
        WidgetKind.Card -> PrayerCardWidgetReceiver::class.java
        WidgetKind.Minimal -> MinimalWidgetReceiver::class.java
        WidgetKind.Current -> PrayerNextWidgetReceiver::class.java
        WidgetKind.Tile -> PrayerTileWidgetReceiver::class.java
        WidgetKind.Icon -> PrayerIconWidgetReceiver::class.java
    }
    val ctx = AppCtx.context
    val mgr = ctx.getSystemService(AppWidgetManager::class.java) ?: return
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mgr.isRequestPinAppWidgetSupported) {
        mgr.requestPinAppWidget(ComponentName(ctx, receiver), null, null)
    }
}
