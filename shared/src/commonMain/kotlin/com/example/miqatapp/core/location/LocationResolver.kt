package com.example.miqatapp.core.location

import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The location move-check: was a fresh GPS fix a real move, and if so, which city? Resolves coords to the
 * nearest bundled city, fully offline. To go online later, swap the body of [resolve] for a network geocoder.
 */
object LocationResolver {
    const val MOVE_THRESHOLD_KM = 32.0   // ~20 miles — "you changed city", not GPS drift or moving around town

    /**
     * A candidate [Place] when [fix] is a meaningful move from [current] and resolves to a different city,
     * else null. The distance gate is a two-point calc; the catalog only loads once the gate passes.
     */
    suspend fun detectMove(current: Place, fix: Coordinates): Place? {
        if (distanceKm(fix, current) < MOVE_THRESHOLD_KM) return null
        val candidate = resolve(fix) ?: return null
        return candidate.takeIf { it.name != current.name || it.countryCode != current.countryCode }
    }

    /** Coords → nearest catalog city (offline). Swap this body for a network reverse-geocoder to go online. */
    private suspend fun resolve(coords: Coordinates): Place? =
        catalog().nearestTo(coords.latitude, coords.longitude)

    // the 49k-row catalog, loaded once off the main thread, then cached
    private var catalog: List<Place>? = null
    private val loadLock = Mutex()
    private suspend fun catalog(): List<Place> {
        catalog?.let { return it }
        return loadLock.withLock {
            catalog ?: withContext(Dispatchers.Default) { Place.fromCatalog(Res.readBytes("files/cities.txt")) }
                .also { catalog = it }
        }
    }
}
