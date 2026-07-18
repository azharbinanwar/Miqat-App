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

// Prayer Chip (design 19i) — 1×1, app-icon size. Corner "Next" + name + time for the next prayer.
class PrayerChipWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rv = loadSnapshot()?.let { chipRemoteViews(context, it, WidgetConfig.opacity(), WidgetConfig.color()) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { AndroidRemoteViews(chipRemoteViews(context, sampleSnapshot(), 1f, WidgetColor.default), GlanceModifier.fillMaxSize()) }
    }
}

class PrayerChipWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerChipWidget()
}

private fun chipRemoteViews(ctx: Context, snap: WidgetSnapshot, opacity: Float, color: WidgetColor): RemoteViews {
    val head = headState(snap, System.currentTimeMillis())
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_chip_widget", "layout"))
    widgetChrome(rv, ctx, color, opacity)
    for (v in listOf("cornerlabel", "name", "time")) rv.setTextColor(viewId(ctx, v), on)
    rv.setTextViewText(viewId(ctx, "name"), head.next.label)
    rv.setTextViewText(viewId(ctx, "time"), head.next.timeText.substringBefore(' '))
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
