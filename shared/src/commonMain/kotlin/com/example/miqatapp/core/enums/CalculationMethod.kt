package com.example.miqatapp.core.enums

/**
 * Prayer-time calculation method. Carries its Fajr/Isha twilight angles (and the
 * Isha interval for methods that use minutes-after-Maghrib instead of an angle).
 * Ported from the macOS app.
 */
enum class CalculationMethod(
    val label: String,
    val shortName: String,          // concise name for compact UI (pickers, the location-move sheet)
    val region: String,
    val fajrAngle: Double,
    val ishaAngle: Double?,        // null when [ishaIntervalMinutes] is used
    val ishaIntervalMinutes: Int? = null,
    val countries: Set<String> = emptySet(),   // ISO alpha-2 codes that use this method; empty = opt-in only
) {
    MWL("Muslim World League", "MWL", "Global", 18.0, 17.0),
    ISNA("Islamic Society of North America", "ISNA", "North America", 15.0, 15.0, countries = setOf("US", "CA")),
    Egypt("Egyptian General Authority", "Egypt", "Africa", 19.5, 17.5, countries = setOf("EG", "SY", "SD", "LY", "IQ", "JO", "LB", "PS", "YE")),
    Makkah("Umm al-Qura", "Umm al-Qura", "Saudi Arabia", 18.5, ishaAngle = null, ishaIntervalMinutes = 90, countries = setOf("SA")),
    Karachi("University of Islamic Sciences, Karachi", "Karachi", "South Asia", 18.0, 18.0, countries = setOf("PK", "IN", "BD", "AF", "LK")),
    Turkey("Diyanet İşleri", "Diyanet", "Turkey", 18.0, 17.0, countries = setOf("TR")),
    Moonsighting("Moonsighting Committee", "Moonsighting", "Global", 18.0, 18.0),
    Singapore("MUIS Singapore", "MUIS", "Singapore", 20.0, 18.0, countries = setOf("SG")),
    Dubai("Dubai / UAE", "Dubai", "UAE", 18.2, 18.2, countries = setOf("AE")),
    Kuwait("Kuwait", "Kuwait", "Kuwait", 18.0, 17.5, countries = setOf("KW")),
    Qatar("Qatar", "Qatar", "Qatar", 18.0, ishaAngle = null, ishaIntervalMinutes = 90, countries = setOf("QA")),
    Gulf("Gulf Region", "Gulf", "Gulf", 19.5, ishaAngle = null, ishaIntervalMinutes = 90, countries = setOf("BH", "OM")),
    France("Union des Organisations Islamiques de France", "UOIF", "France", 12.0, 12.0, countries = setOf("FR")),
    Russia("Spiritual Administration of Muslims of Russia", "Russia", "Russia", 16.0, 15.0, countries = setOf("RU")),
    Malaysia("Jabatan Kemajuan Islam Malaysia (JAKIM)", "JAKIM", "Malaysia", 20.0, 18.0, countries = setOf("MY")),
    Indonesia("Kementerian Agama Republik Indonesia", "Kemenag", "Indonesia", 20.0, 18.0, countries = setOf("ID")),
    Tunisia("Tunisia", "Tunisia", "Tunisia", 18.0, 18.0, countries = setOf("TN")),
    Algeria("Algeria", "Algeria", "Algeria", 19.0, 17.0, countries = setOf("DZ")),
    Morocco("Morocco", "Morocco", "Morocco", 19.0, 17.0, countries = setOf("MA")),
    Portugal("Comunidade Islâmica de Lisboa", "Lisbon", "Portugal", 18.0, ishaAngle = null, ishaIntervalMinutes = 77, countries = setOf("PT")),
    Tehran("Institute of Geophysics, Tehran", "Tehran", "Iran", 17.7, 14.0, countries = setOf("IR")),
    Jafari("Shia Ithna-Ashari (Jafari)", "Jafari", "Shia", 16.0, 14.0),
    // angles are user-set (see Prefs.CUSTOM_*); the defaults here are just the seed shown first.
    Custom("Custom", "Custom", "Custom angles", 18.0, 17.0);

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name }

        /** Official method for an ISO country code; MWL for anywhere unmapped (the global default). */
        fun forCountry(code: String?): CalculationMethod {
            val cc = code?.uppercase() ?: return MWL
            return entries.firstOrNull { cc in it.countries } ?: MWL
        }
    }
}

/** Everything searchable for a method — name, short name, region, and country names. */
val CalculationMethod.searchText: String
    get() = "$label $shortName $region " + countries.joinToString(" ") { countryLabel(it) }
