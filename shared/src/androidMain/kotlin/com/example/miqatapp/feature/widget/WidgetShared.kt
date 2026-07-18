package com.example.miqatapp.feature.widget

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.composables.icons.lucide.CloudSun
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.SunMedium
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.WidgetColor
import kotlinx.serialization.json.Json

// The segment's own glyph. Keyed on the enum's Lucide icon (single source) — RemoteViews needs an Android drawable,
// so each Lucide vector has a hand-copied twin in res/drawable.
fun badgeIconRes(ctx: Context, key: String): Int {
    val name = when (Miqat.entries.firstOrNull { it.key == key }?.icon) {
        Lucide.Sunrise -> "ic_miqat_sunrise"
        Lucide.SunMedium -> "ic_miqat_sun_medium"
        Lucide.Sun -> "ic_miqat_sun"
        Lucide.CloudSun -> "ic_miqat_cloud_sun"
        Lucide.Sunset -> "ic_miqat_sunset"
        else -> "ic_miqat_moon" // Moon: Imsak, Isha, Midnight, LastThird
    }
    return viewId(ctx, name, "drawable")
}

// Shared chrome for every prayer widget: the colour gradient background (faded by opacity) + the faint app mark,
// both tinted from the picked WidgetColor. Every layout has ids `bg` and `watermark`.
fun widgetChrome(rv: RemoteViews, ctx: Context, color: WidgetColor, opacity: Float) {
    rv.setImageViewBitmap(viewId(ctx, "bg"), gradientBitmap(ctx, 300, 160, color.fill.toArgb(), color.fillEnd.toArgb()))
    rv.setInt(viewId(ctx, "bg"), "setImageAlpha", (opacity.coerceIn(0f, 1f) * 255).toInt())
    rv.setInt(viewId(ctx, "watermark"), "setColorFilter", color.on.toArgb())
}

// The stored snapshot, or a freshly-built one if none exists yet. Shared by every widget's provideGlance.
fun loadSnapshot(): WidgetSnapshot? {
    WidgetStore.read()?.let { runCatching { Json.decodeFromString<WidgetSnapshot>(it) }.getOrNull() }?.let { return it }
    WidgetPublisher.refresh()
    return WidgetStore.read()?.let { runCatching { Json.decodeFromString<WidgetSnapshot>(it) }.getOrNull() }
}

// Fixed sample for the widget-picker preview (providePreview, Android 15+) — no store needed.
fun sampleSnapshot(): WidgetSnapshot {
    val now = System.currentTimeMillis()
    fun p(key: String, mins: Long, t: String, label: String, ar: String) = WidgetPrayer(key, now + mins * 60_000, t, label, ar)
    val prayers = listOf(
        p("fajr", -300, "3:35 AM", "Fajr", "الفجر"), p("dhuhr", 120, "12:34 PM", "Dhuhr", "الظهر"),
        p("asr", 300, "3:58 PM", "Asr", "العصر"), p("maghrib", 480, "6:47 PM", "Maghrib", "المغرب"), p("isha", 620, "8:12 PM", "Isha", "العشاء"),
    )
    val segments = listOf(
        p("fajr", -300, "3:35 AM", "Fajr", "الفجر"), p("sunrise", -240, "5:41 AM", "Sunrise", "الشروق"),
        p("ishraq", -180, "6:05 AM", "Ishraq", "الإشراق"), p("dhuhr", 120, "12:34 PM", "Dhuhr", "الظهر"),
        p("asr", 300, "3:58 PM", "Asr", "العصر"), p("maghrib", 480, "6:47 PM", "Maghrib", "المغرب"), p("isha", 620, "8:12 PM", "Isha", "العشاء"),
    )
    return WidgetSnapshot(
        dateIso = "", locationName = "Makkah", hijri = "3 Safar", dayName = "Friday",
        prayers = prayers, segments = segments,
        prevIsha = p("isha", -800, "8:12 PM", "Isha", "العشاء"),
        nextFajr = p("fajr", 900, "3:35 AM", "Fajr", "الفجر"),
    )
}

// Draw-time state shared by every widget: which prayer is now, which is next, and how far through the gap we are.
data class WState(val current: WidgetPrayer, val next: WidgetPrayer, val prog: Float)

fun wstate(snap: WidgetSnapshot, now: Long): WState {
    val next = snap.prayers.firstOrNull { it.atMillis > now } ?: snap.nextFajr
    val current = (listOf(snap.prevIsha) + snap.prayers).filter { it.atMillis <= now }.lastOrNull() ?: snap.prevIsha
    val span = (next.atMillis - current.atMillis).coerceAtLeast(1)
    return WState(current, next, ((now - current.atMillis).toFloat() / span).coerceIn(0f, 1f))
}

// Header state: the real day-segment we're in now (Miqat.PERIODS, so Sunrise/Ishraq are named, not held as Fajr) + the next.
fun headState(snap: WidgetSnapshot, now: Long): WState {
    val next = snap.segments.firstOrNull { it.atMillis > now } ?: snap.nextFajr
    val current = snap.segments.filter { it.atMillis <= now }.lastOrNull() ?: snap.prevIsha
    val span = (next.atMillis - current.atMillis).coerceAtLeast(1)
    return WState(current, next, ((now - current.atMillis).toFloat() / span).coerceIn(0f, 1f))
}

// "1:33" / "0:43" — colon format, matching the live Chronometer's countdown.
fun countdown(deltaMs: Long): String {
    val d = if (deltaMs < 0) 0 else deltaMs
    val h = (d / 3_600_000).toInt()
    val m = ((d % 3_600_000) / 60_000).toInt()
    return "$h:${m.toString().padStart(2, '0')}"
}

// Static countdown text: "in 2h 14m" while counting, "now" once reached — never negative.
fun countdownLabel(deltaMs: Long): String = if (deltaMs <= 0) "now" else "in ${countdown(deltaMs)}"

// The appWidgetId behind a GlanceId — the key into per-instance WidgetConfig.
fun styleId(ctx: Context, glanceId: GlanceId): Int =
    runCatching { GlanceAppWidgetManager(ctx).getAppWidgetId(glanceId) }.getOrDefault(0)

// Resolve a res id by name (widgets live in androidMain but resources in the app module — no R here).
fun viewId(ctx: Context, name: String, type: String = "id") = ctx.resources.getIdentifier(name, type, ctx.packageName)

// Tap → open the app.
fun launchPendingIntent(ctx: Context): PendingIntent {
    val launch = ctx.packageManager.getLaunchIntentForPackage(ctx.packageName)
    return PendingIntent.getActivity(ctx, 0, launch, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

// Plain diagonal gradient (top-left → bottom-right) as a bitmap — the Prayer Card's background fill.
// System rounds the widget corners on 12+; opacity is applied on the ImageView (setImageAlpha).
fun gradientBitmap(ctx: Context, wDp: Int, hDp: Int, top: Int, bottom: Int): Bitmap {
    val dm = ctx.resources.displayMetrics.density
    val w = (wDp * dm).toInt().coerceAtLeast(1)
    val h = (hDp * dm).toInt().coerceAtLeast(1)
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    Canvas(bmp).drawRect(0f, 0f, w.toFloat(), h.toFloat(), Paint().apply {
        shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), top, bottom, Shader.TileMode.CLAMP)
    })
    return bmp
}
