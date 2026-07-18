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

// Prayer Tile (design 19f) — 2×2. Current Miqat icon + name (small, above) + the current prayer's end time (big).
class PrayerTileWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rv = loadSnapshot()?.let { tileRemoteViews(context, it, WidgetConfig.opacity(), WidgetConfig.color()) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        provideContent { AndroidRemoteViews(tileRemoteViews(context, sampleSnapshot(), 1f, WidgetColor.default), GlanceModifier.fillMaxSize()) }
    }
}

class PrayerTileWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerTileWidget()
}

private fun tileRemoteViews(ctx: Context, snap: WidgetSnapshot, opacity: Float, color: WidgetColor): RemoteViews {
    val head = headState(snap, System.currentTimeMillis())
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_tile_widget", "layout"))
    widgetChrome(rv, ctx, color, opacity)
    for (v in listOf("name", "time")) rv.setTextColor(viewId(ctx, v), on)
    rv.setImageViewResource(viewId(ctx, "icon"), badgeIconRes(ctx, head.current.key))
    rv.setInt(viewId(ctx, "icon"), "setColorFilter", on)
    rv.setTextViewText(viewId(ctx, "name"), head.current.label.uppercase())
    rv.setTextViewText(viewId(ctx, "time"), head.next.timeText.substringBefore(' ')) // current prayer's end = next slot's start
    rv.setOnClickPendingIntent(viewId(ctx, "root"), launchPendingIntent(ctx))
    return rv
}
