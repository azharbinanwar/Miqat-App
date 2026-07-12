package com.example.miqatapp.core.constants.defaults

import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.enums.AdhanRoundingStyle
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab

/**
 * Ship defaults for prayer calculation — the values a brand-new user gets before touching Settings.
 *
 * Read ONLY by the Miqat engine, as the fallback when Prefs has no saved value (`Prefs.x ?: MiqatDefaults.x`).
 * Prefs (storage) must NOT import this — it stays domain-free and returns null when unset. Keeping the default
 * here (not seeded into Prefs at launch) means a future app update can improve a default and it still reaches
 * every user who never overrode it.
 */
object MiqatDefaults {
    // Ship defaults. The method default is derived from the place's country, so it isn't a constant here.
    val madhab = Madhab.Hanafi
    val highLatRule = HighLatRule.MiddleNight
    val rounding = AdhanRoundingStyle.Nearest

    /** Seed angles for the Custom method (whole degrees), shown until the user sets their own. */
    const val FAJR_ANGLE = 18
    const val ISHA_ANGLE = 17
    val angleRange = 10..21                    // one source for the Settings stepper bounds and the engine's sanity check

    /** Per-Miqat ± minute tweak (local mosque / sighting differs from the computed time). 0 == unset == default. */
    const val MINUTE_ADJUSTMENT = 0
    val adjustmentRange = -30..30

    /** Offsets for the derived points (minutes): Imsak before Fajr, Ishraq after Sunrise. */
    const val IMSAK_OFFSET_MIN = 10
    const val ISHRAQ_OFFSET_MIN = 15

    /** Suggested starting locations (shown on the Locations screen); the first is the pre-GPS fallback. */
    val places = listOf(
        Place("Makkah", "SA", 21.4225, 39.8262, "Asia/Riyadh"),
        Place("Madinah", "SA", 24.4672, 39.6111, "Asia/Riyadh"),
        Place("Karachi", "PK", 24.8607, 67.0011, "Asia/Karachi"),
    )
    val fallbackPlace = places.first()         // Makkah — before GPS / city is set
}
