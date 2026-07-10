package com.example.miqatapp.core.constants

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A location prayer times are computed for. The single model used everywhere — presets, city search, GPS,
 * and the saved/active selection. [latitude]/[longitude]/[timeZone] are what the calc engine needs;
 * [countryCode] (ISO alpha-2) drives the country-derived default method and the display label.
 *
 * [ascii] is a diacritic-free search helper from the catalog (search only — not persisted, defaults to [name]).
 */
@Serializable
data class Place(
    val name: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
    @Transient val ascii: String = name,
)

/** Preset locations — quick picks before/without GPS. */
object Places {
    val Makkah = Place("Makkah", "SA", 21.4225, 39.8262, "Asia/Riyadh")
    val Madinah = Place("Madinah", "SA", 24.4672, 39.6111, "Asia/Riyadh")
    val Karachi = Place("Karachi", "PK", 24.8607, 67.0011, "Asia/Karachi")

    val presets = listOf(Makkah, Madinah, Karachi)
    val default = Makkah
}
