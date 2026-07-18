package com.example.miqatapp.feature.widget

import android.content.Context
import android.os.Build
import android.util.TypedValue
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

// Prayer Icon (design 19f, time-first) — 1×1 app-icon tile (rounded square, 8dp). Current prayer name + its end time.
class PrayerIconWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val style = WidgetConfig.claim(styleId(context, id))
        val rv = loadSnapshot()?.let { iconRemoteViews(context, it, style) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { AndroidRemoteViews(iconRemoteViews(context, sampleSnapshot(), WidgetStyle()), GlanceModifier.fillMaxSize()) }
    }
}

class PrayerIconWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerIconWidget()
}

internal fun iconRemoteViews(ctx: Context, snap: WidgetSnapshot, style: WidgetStyle): RemoteViews {
    val head = headState(snap, System.currentTimeMillis())
    val color = style.color
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_icon_widget", "layout"))
    rv.setImageViewBitmap(viewId(ctx, "bg"), gradientBitmap(ctx, 120, 120, color.fill.toArgb(), color.fillEnd.toArgb()))
    rv.setInt(viewId(ctx, "bg"), "setImageAlpha", (style.alpha * 255).toInt())
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        rv.setViewOutlinePreferredRadius(viewId(ctx, "root"), 8f, TypedValue.COMPLEX_UNIT_DIP)
    }
    for (v in listOf("name", "time")) rv.setTextColor(viewId(ctx, v), on)
    rv.setTextViewText(viewId(ctx, "name"), head.current.label.uppercase())
    rv.setTextViewText(viewId(ctx, "time"), head.next.timeText.substringBefore(' ')) // current prayer's end = next slot's start
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
