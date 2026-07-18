package com.example.miqatapp.feature.widget

import kotlinx.serialization.Serializable

/**
 * The flat, widget-readable view of today. Written whenever times/settings change; read by the platform
 * widget at draw time. Display strings are baked in (locale + clock format already correct at write),
 * so the widget only formats the countdown (neutral HH:MM) from [WidgetPrayer.atMillis]. Shared with iOS later.
 */
@Serializable
data class WidgetSnapshot(
    val dateIso: String,
    val locationName: String,        // active place / city, shown as the address line
    val hijri: String,               // today's Hijri date, e.g. "30 Muharram" (from SettingsStore.hijriDate)
    val dayName: String,             // localized weekday, e.g. "Friday" (from DayOfWeek.labelRes)
    val prayers: List<WidgetPrayer>, // the five: Fajr, Dhuhr, Asr, Maghrib, Isha (enum order) — the footer
    val segments: List<WidgetPrayer>,// the live "now" markers (Miqat.PERIODS): + Sunrise/Ishraq — drives the header
    val prevIsha: WidgetPrayer,      // yesterday's Isha — "current" before today's Fajr
    val nextFajr: WidgetPrayer,      // tomorrow's Fajr — the countdown target after Isha passes
)

@Serializable
data class WidgetPrayer(
    val key: String,      // Miqat.key: fajr, dhuhr, asr, maghrib, isha — drives the widget's color pick
    val atMillis: Long,   // absolute instant, so a draw is correct without recompute
    val timeText: String, // pre-formatted "4:41 PM" / "16:41"
    val label: String,    // localized name (Jumu'ah on Friday Dhuhr)
    val ar: String,       // Arabic-script name, resolved from values-ar (single source)
)
