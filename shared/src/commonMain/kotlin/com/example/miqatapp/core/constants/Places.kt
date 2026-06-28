package com.example.miqatapp.core.constants

/** A location with coordinates. */
data class Place(val name: String, val latitude: Double, val longitude: Double)

/** Preset locations (from the spec) — used as quick picks before/without GPS. */
object Places {
    val Makkah = Place("Makkah", 21.3891, 39.8579)
    val Madinah = Place("Madinah", 24.5247, 39.5692)
    val Karachi = Place("Karachi", 24.8607, 67.0011)

    val presets = listOf(Makkah, Madinah, Karachi)
    val default = Makkah
}
