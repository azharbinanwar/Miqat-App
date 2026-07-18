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
import com.example.miqatapp.core.enums.WidgetColor

// Current Prayer (design 19d) — 2×2. Centred NOW / current prayer / time.
class PrayerNextWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rv = loadSnapshot()?.let { nextRemoteViews(context, it, WidgetConfig.opacity(), WidgetConfig.color()) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { AndroidRemoteViews(nextRemoteViews(context, sampleSnapshot(), 1f, WidgetColor.default), GlanceModifier.fillMaxSize()) }
    }
}

class PrayerNextWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerNextWidget()
}

private fun nextRemoteViews(ctx: Context, snap: WidgetSnapshot, opacity: Float, color: WidgetColor): RemoteViews {
    val head = headState(snap, System.currentTimeMillis())
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_next_widget", "layout"))
    widgetChrome(rv, ctx, color, opacity)
    for (v in listOf("label", "name", "time")) rv.setTextColor(viewId(ctx, v), on)
    rv.setTextViewText(viewId(ctx, "name"), head.current.label)
    rv.setTextViewText(viewId(ctx, "time"), head.current.timeText)
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
