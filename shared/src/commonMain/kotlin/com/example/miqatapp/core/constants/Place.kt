package com.example.miqatapp.core.constants

import com.example.miqatapp.core.enums.countryLabel
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * A location prayer times are computed for. The single model used everywhere — suggested list, city search,
 * GPS, and the saved/active selection. [latitude]/[longitude]/[timeZone] feed the calc engine; [countryCode]
 * (ISO alpha-2) drives the country-derived method and the display label.
 *
 * [ascii] is a diacritic-free search helper from the catalog (search only — not persisted, defaults to [name]).
 */
@Serializable
data class Place(
    val name: String,
    val countryCode: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String,
    @Transient val ascii: String = name,
) {
    companion object {
        /** Bundled GeoNames-style TSV (name, asciiname, lat, lng, countryCode, timezone) → Places. */
        fun fromCatalog(bytes: ByteArray): List<Place> =
            bytes.decodeToString().lineSequence().mapNotNull { line ->
                if (line.isBlank()) return@mapNotNull null
                val p = line.split('\t')
                if (p.size < 6) return@mapNotNull null
                val lat = p[2].toDoubleOrNull() ?: return@mapNotNull null
                val lng = p[3].toDoubleOrNull() ?: return@mapNotNull null
                Place(name = p[0], countryCode = p[4], latitude = lat, longitude = lng, timeZone = p[5], ascii = p[1])
            }.toList()
    }
}

/** Display country label (e.g. "Pakistan, PK"), derived from countryCode — not stored, re-localizes. */
val Place.countryLabel: String get() = countryLabel(countryCode)
