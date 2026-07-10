package com.example.miqatapp.core.location

import androidx.compose.runtime.Composable
import com.example.miqatapp.core.constants.Place
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A raw GPS fix. */
data class Coordinates(val latitude: Double, val longitude: Double)

/**
 * Device location. Assumes the Location permission is already granted (request it via PermissionService
 * first). Returns null when there's no fix available or permission was refused.
 */
interface GeoLocator {
    suspend fun current(): Coordinates?
}

/** Platform-backed [GeoLocator], bound to the current Compose context. */
@Composable
expect fun rememberGeoLocator(): GeoLocator

/**
 * Nearest catalog city to a GPS fix — turns raw coords into a full [Place] (name + country + timezone)
 * fully offline, reusing the bundled catalog. City-level accuracy is exactly right for prayer times.
 */
fun List<Place>.nearestTo(latitude: Double, longitude: Double): Place? =
    minByOrNull { haversineKm(latitude, longitude, it.latitude, it.longitude) }

private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = (lat2 - lat1).toRadians()
    val dLon = (lon2 - lon1).toRadians()
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLon / 2) * sin(dLon / 2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun Double.toRadians() = this * kotlin.math.PI / 180.0
