package com.example.miqatapp.feature.widget

import android.content.Context
import android.os.SystemClock
import android.view.View
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

// Minimal widget (2×2). Native RemoteViews, current prayer + live countdown. Shares the card's colour + opacity.
class MinimalWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val style = WidgetConfig.claim(styleId(context, id))
        val rv = loadSnapshot()?.let { minimalRemoteViews(context, it, live = true, style) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val rv = minimalRemoteViews(context, sampleSnapshot(), live = false, WidgetStyle())
        provideContent { AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
}

class MinimalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = MinimalWidget()
}

internal fun minimalRemoteViews(ctx: Context, snap: WidgetSnapshot, live: Boolean, style: WidgetStyle): RemoteViews {
    val now = System.currentTimeMillis()
    val head = headState(snap, now) // current/next step through Miqat.SLOTS
    val color = style.color
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "minimal_widget", "layout"))

    rv.setImageViewBitmap(viewId(ctx, "bg"), gradientBitmap(ctx, 220, 220, color.fill.toArgb(), color.fillEnd.toArgb()))
    rv.setInt(viewId(ctx, "bg"), "setImageAlpha", (style.alpha * 255).toInt())
    rv.setInt(viewId(ctx, "watermark"), "setColorFilter", on)
    for (v in listOf("label", "name", "next", "chrono", "chronoStatic")) rv.setTextColor(viewId(ctx, v), on)

    rv.setTextViewText(viewId(ctx, "name"), head.current.label)
    rv.setTextViewText(viewId(ctx, "next"), "Next · ${head.next.label} · ${head.next.timeText.substringBefore(' ')}")

    val chrono = viewId(ctx, "chrono")
    val chronoStatic = viewId(ctx, "chronoStatic")
    if (live) {
        rv.setViewVisibility(chrono, View.VISIBLE)
        rv.setViewVisibility(chronoStatic, View.GONE)
        val base = SystemClock.elapsedRealtime() + (head.next.atMillis - now)
        rv.setChronometerCountDown(chrono, true)
        rv.setChronometer(chrono, base, "in %s", true)
    } else {
        rv.setViewVisibility(chrono, View.GONE)
        rv.setViewVisibility(chronoStatic, View.VISIBLE)
        rv.setTextViewText(chronoStatic, countdownLabel(head.next.atMillis - now))
    }
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
