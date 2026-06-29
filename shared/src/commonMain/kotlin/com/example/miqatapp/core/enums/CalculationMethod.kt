package com.example.miqatapp.core.enums

/**
 * Prayer-time calculation method. Carries its Fajr/Isha twilight angles (and the
 * Isha interval for methods that use minutes-after-Maghrib instead of an angle).
 * Ported from the macOS app.
 */
enum class CalculationMethod(
    val label: String,
    val region: String,
    val fajrAngle: Double,
    val ishaAngle: Double?,        // null when [ishaIntervalMinutes] is used
    val ishaIntervalMinutes: Int? = null,
) {
    MWL("Muslim World League", "Global", 18.0, 17.0),
    ISNA("Islamic Society of North America", "North America", 15.0, 15.0),
    Egypt("Egyptian General Authority", "Africa", 19.5, 17.5),
    Makkah("Umm al-Qura", "Saudi Arabia", 18.5, ishaAngle = null, ishaIntervalMinutes = 90),
    Karachi("University of Islamic Sciences, Karachi", "South Asia", 18.0, 18.0),
    Turkey("Diyanet İşleri", "Turkey", 18.0, 17.0),
    Moonsighting("Moonsighting Committee", "Global", 18.0, 18.0),
    Singapore("MUIS Singapore", "Singapore", 20.0, 18.0),
    Dubai("Dubai / UAE", "UAE", 18.2, 18.2),
    Tehran("Institute of Geophysics, Tehran", "Iran", 17.7, 14.0);

    companion object {
        val default = MWL
    }
}
