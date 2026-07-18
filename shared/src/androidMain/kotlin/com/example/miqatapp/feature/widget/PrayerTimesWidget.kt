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
import com.example.miqatapp.core.enums.WidgetColor

// Prayer Times widget — one row. Day · Hijri, current prayer big (left), next + countdown (right).
// Current/next step through the day's slots (Miqat.SLOTS). Shares the card's colour + opacity.
class PrayerTimesWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rv = loadSnapshot()?.let { prayerTimesRemoteViews(context, it, live = true, WidgetConfig.opacity(), WidgetConfig.color()) }
        provideContent { if (rv != null) AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val rv = prayerTimesRemoteViews(context, sampleSnapshot(), live = false, opacity = 1f, color = WidgetColor.default)
        provideContent { AndroidRemoteViews(rv, GlanceModifier.fillMaxSize()) }
    }
}

class PrayerTimesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = PrayerTimesWidget()
}

private fun prayerTimesRemoteViews(ctx: Context, snap: WidgetSnapshot, live: Boolean, opacity: Float, color: WidgetColor): RemoteViews {
    val now = System.currentTimeMillis()
    val head = headState(snap, now) // current/next step through Miqat.SLOTS
    val on = color.on.toArgb()
    val rv = RemoteViews(ctx.packageName, viewId(ctx, "prayer_times_widget", "layout"))

    rv.setImageViewBitmap(viewId(ctx, "bg"), gradientBitmap(ctx, 360, 150, color.fill.toArgb(), color.fillEnd.toArgb()))
    rv.setInt(viewId(ctx, "bg"), "setImageAlpha", (opacity.coerceIn(0f, 1f) * 255).toInt())
    rv.setInt(viewId(ctx, "watermark"), "setColorFilter", on)
    for (v in listOf("hijri", "curname", "nextlabel", "nextname", "nexttime", "chrono", "chronoStatic")) rv.setTextColor(viewId(ctx, v), on)

    rv.setTextViewText(viewId(ctx, "hijri"), "${snap.dayName} · ${snap.hijri}")
    rv.setTextViewText(viewId(ctx, "curname"), head.current.label)
    rv.setTextViewText(viewId(ctx, "nextname"), head.next.label)
    rv.setTextViewText(viewId(ctx, "nexttime"), "${head.next.timeText} · ")

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
