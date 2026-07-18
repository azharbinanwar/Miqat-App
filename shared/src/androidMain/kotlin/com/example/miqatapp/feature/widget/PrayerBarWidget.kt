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

// Prayer Bar (design 23e) — 4×1. NOW current · NEXT next with a centre divider, time only.
// Shares the colour + opacity picker and the watermark; current/next step through Miqat.SLOTS.
class PrayerBarWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rv = loadSnapshot()?.let { barRemoteViews(context, it, WidgetConfig.opacity(), WidgetConfig.color()) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { AndroidRemoteViews(barRemoteViews(context, sampleSnapshot(), 1f, WidgetColor.default), GlanceModifier.fillMaxSize()) }
    }
}

class PrayerBarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerBarWidget()
}

private fun barRemoteViews(ctx: Context, snap: WidgetSnapshot, opacity: Float, color: WidgetColor): RemoteViews {
    val head = headState(snap, System.currentTimeMillis())
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_bar_widget", "layout"))
    widgetChrome(rv, ctx, color, opacity)
    for (v in listOf("nowlabel", "nowval", "nextlabel", "nextval")) rv.setTextColor(viewId(ctx, v), on)
    rv.setInt(viewId(ctx, "divider"), "setBackgroundColor", (on and 0x00FFFFFF) or 0x33000000)
    rv.setTextViewText(viewId(ctx, "nowval"), "${head.current.label} · ${head.current.timeText}")
    rv.setTextViewText(viewId(ctx, "nextval"), "${head.next.label} · ${head.next.timeText}")
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
