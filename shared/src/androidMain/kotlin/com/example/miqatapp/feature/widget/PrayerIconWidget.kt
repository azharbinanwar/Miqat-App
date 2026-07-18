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

// Prayer Icon (design 19f, time-first) — 1×1 round app-icon badge. Current prayer name (small) + its end time (big).
class PrayerIconWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rv = loadSnapshot()?.let { iconRemoteViews(context, it, WidgetConfig.opacity(), WidgetConfig.color()) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { AndroidRemoteViews(iconRemoteViews(context, sampleSnapshot(), 1f, WidgetColor.default), GlanceModifier.fillMaxSize()) }
    }
}

class PrayerIconWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerIconWidget()
}

private fun iconRemoteViews(ctx: Context, snap: WidgetSnapshot, opacity: Float, color: WidgetColor): RemoteViews {
    val head = headState(snap, System.currentTimeMillis())
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_icon_widget", "layout"))
    rv.setImageViewBitmap(viewId(ctx, "bg"), circleGradientBitmap(ctx, 120, color.fill.toArgb(), color.fillEnd.toArgb()))
    rv.setInt(viewId(ctx, "bg"), "setImageAlpha", (opacity.coerceIn(0f, 1f) * 255).toInt())
    for (v in listOf("name", "time")) rv.setTextColor(viewId(ctx, v), on)
    rv.setTextViewText(viewId(ctx, "name"), head.current.label.uppercase())
    rv.setTextViewText(viewId(ctx, "time"), head.next.timeText.substringBefore(' ')) // current prayer's end = next slot's start
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
