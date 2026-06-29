package com.example.miqatapp.core.enums

/** High-latitude rule for Fajr/Isha when the sun doesn't reach the required angle. */
enum class HighLatRule(val label: String) {
    MiddleNight("Middle of the Night"),
    SeventhNight("One-Seventh of the Night"),
    AngleBased("Angle-Based"),
}
