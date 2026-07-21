package com.example.miqatapp.core.constants.defaults

/**
 * Ship defaults for the Quran reader — the values a new user gets before touching anything.
 * Read by `QuranStore` (as the fallback when `PrefsService` has no saved value) and by the reader UI
 * for the font-size range. Mirrors [SettingsDefaults].
 */
object QuranDefaults {
    const val FONT_SP = 15        // default reading size
    const val MIN_FONT_SP = 12    // stepper floor
    const val MAX_FONT_SP = 40    // stepper ceiling
}
