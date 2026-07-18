package com.example.miqatapp.feature.widget

import android.content.Context
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.fillMaxSize

// Current Prayer (design 19d) — 2×2. Centred NOW / current prayer / time.
class PrayerNextWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val style = WidgetConfig.claim(styleId(context, id))
        val rv = loadSnapshot()?.let { nextRemoteViews(context, it, style) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { AndroidRemoteViews(nextRemoteViews(context, sampleSnapshot(), WidgetStyle()), GlanceModifier.fillMaxSize()) }
    }
}

class PrayerNextWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerNextWidget()
}

internal fun nextRemoteViews(ctx: Context, snap: WidgetSnapshot, style: WidgetStyle): RemoteViews {
    val head = headState(snap, System.currentTimeMillis())
    val color = style.color
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_next_widget", "layout"))
    widgetChrome(rv, ctx, color, style.alpha)
    for (v in listOf("label", "name", "time")) rv.setTextColor(viewId(ctx, v), on)
    rv.setTextViewText(viewId(ctx, "name"), head.current.label)
    rv.setTextViewText(viewId(ctx, "time"), head.current.timeText)
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
