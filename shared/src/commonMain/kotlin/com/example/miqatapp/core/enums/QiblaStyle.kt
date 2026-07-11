package com.example.miqatapp.core.enums

/** Compass dial styles the user can switch between on the Qibla screen. */
enum class QiblaStyle(val label: String) {
    Modern("Modern"),
    Classic("Classic"),
    CompassRose("Compass rose");

    companion object {
        fun fromName(name: String?): QiblaStyle? = entries.firstOrNull { it.name == name }
    }
}
