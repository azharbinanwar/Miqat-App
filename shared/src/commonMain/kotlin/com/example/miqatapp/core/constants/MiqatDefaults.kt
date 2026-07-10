package com.example.miqatapp.core.constants

import com.example.miqatapp.core.enums.AdhanRoundingStyle
import com.example.miqatapp.core.enums.CalculationMethod
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
    val method = CalculationMethod.default    // MWL — global fallback. ponytail: country-derived override lands when Place carries an ISO code.
    val madhab = Madhab.default               // Shafi'i
    val highLatRule = HighLatRule.default     // Middle of the Night
    val rounding = AdhanRoundingStyle.Nearest

    /** Seed angles for the Custom method (whole degrees), shown until the user sets their own. */
    const val FAJR_ANGLE = 18
    const val ISHA_ANGLE = 17
    val angleRange = 10..21                    // one source for the Settings stepper bounds and the engine's sanity check

    /** Per-Miqat ± minute tweak (local mosque / sighting differs from the computed time). 0 == unset == default. */
    const val MINUTE_ADJUSTMENT = 0
    val adjustmentRange = -30..30

    /** Location used before GPS / city is set. */
    val place = Places.default                 // Makkah
}
