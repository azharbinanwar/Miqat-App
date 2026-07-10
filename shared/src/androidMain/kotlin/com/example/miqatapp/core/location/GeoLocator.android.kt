package com.example.miqatapp.core.location

import android.content.Context
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberGeoLocator(): GeoLocator {
    val context = LocalContext.current
    return remember(context) { AndroidGeoLocator(context) }
}

private class AndroidGeoLocator(private val context: Context) : GeoLocator {
    override suspend fun current(): Coordinates? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        return try {
            // ponytail: last-known is instant and fine for city-level prayer times. Add a live single-update
            // fallback (requestLocationUpdates / getCurrentLocation) if last-known comes back null on a device.
            val loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            loc?.let { Coordinates(it.latitude, it.longitude) }
        } catch (e: SecurityException) {
            null // permission not granted
        }
    }
}
