package com.example.miqatapp.core.enums

import androidx.compose.ui.graphics.Color

/**
 * The Prayer Card's look: a fixed fill/on-colour pair the user picks (no light/dark theme).
 * [fill]→[fillEnd] is the card's diagonal gradient; [on] is the single text/icon colour used for everything.
 * All values are drawn from the app's emerald palette (see AppColors) and tuned to read well on their own.
 * Names stay English (colour names don't translate clearly).
 */
enum class WidgetColor(
    val label: String,
    val fill: Color,
    val fillEnd: Color,
    val on: Color,
) {
    Emerald("Emerald", Color(0xFF1E7D55), Color(0xFF0A3F2B), Color(0xFFF1FBF6)),
    Ink("Ink green", Color(0xFF16241B), Color(0xFF0C1712), Color(0xFF95D3BA)),
    Pine("Deep pine", Color(0xFF0B4A37), Color(0xFF063020), Color(0xFFB0F0D6)),
    Midnight("Midnight", Color(0xFF1D2021), Color(0xFF111415), Color(0xFFE1E3E4)),
    Mint("Mint", Color(0xFFC7EEDC), Color(0xFFB6EBD2), Color(0xFF003825)),
    Paper("Paper", Color(0xFFFBFCFB), Color(0xFFEFF3F0), Color(0xFF14392B));

    /** Stable key for prefs. */
    val key: String get() = name.lowercase()

    companion object {
        val default = Emerald
        fun fromKey(key: String?): WidgetColor = entries.firstOrNull { it.key == key } ?: default
    }
}
