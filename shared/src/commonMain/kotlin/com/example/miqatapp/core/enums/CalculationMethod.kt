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
    Kuwait("Kuwait", "Kuwait", 18.0, 17.5),
    Qatar("Qatar", "Qatar", 18.0, ishaAngle = null, ishaIntervalMinutes = 90),
    Gulf("Gulf Region", "Gulf", 19.5, ishaAngle = null, ishaIntervalMinutes = 90),
    France("Union des Organisations Islamiques de France", "France", 12.0, 12.0),
    Russia("Spiritual Administration of Muslims of Russia", "Russia", 16.0, 15.0),
    Malaysia("Jabatan Kemajuan Islam Malaysia (JAKIM)", "Malaysia", 20.0, 18.0),
    Indonesia("Kementerian Agama Republik Indonesia", "Indonesia", 20.0, 18.0),
    Tunisia("Tunisia", "Tunisia", 18.0, 18.0),
    Algeria("Algeria", "Algeria", 19.0, 17.0),
    Morocco("Morocco", "Morocco", 19.0, 17.0),
    Portugal("Comunidade Islâmica de Lisboa", "Portugal", 18.0, ishaAngle = null, ishaIntervalMinutes = 77),
    Tehran("Institute of Geophysics, Tehran", "Iran", 17.7, 14.0),
    Jafari("Shia Ithna-Ashari (Jafari)", "Shia", 16.0, 14.0),
    // angles are user-set (see Prefs.CUSTOM_*); the defaults here are just the seed shown first.
    Custom("Custom", "Custom angles", 18.0, 17.0);

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name }
    }
}
