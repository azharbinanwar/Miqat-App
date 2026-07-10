package com.example.miqatapp.core.location

import com.example.miqatapp.core.constants.MiqatDefaults
import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.prefs.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for the user's location — the only `Prefs ?: MiqatDefaults` resolution for place.
 * Same shape as [com.example.miqatapp.feature.prayer.domain.PrayerCalculationRepository]: seed each flow from
 * Prefs once, then every consumer reads the flow; setters persist to Prefs AND emit, so a change from the
 * Location screen (or GPS) updates every observer — drawer header, Home, and the engine later.
 */
object LocationRepository {

    private val _activePlace = MutableStateFlow(Prefs.activePlace ?: MiqatDefaults.place)
    val activePlace: StateFlow<Place> = _activePlace.asStateFlow()

    private val _savedPlaces = MutableStateFlow(Prefs.savedPlaces)
    val savedPlaces: StateFlow<List<Place>> = _savedPlaces.asStateFlow()

    /** Pick a place: add to saved (deduped, newest first) and make it active. */
    fun setActive(place: Place) {
        if (_savedPlaces.value.none { it.sameAs(place) }) {
            val updated = listOf(place) + _savedPlaces.value
            Prefs.savedPlaces = updated
            _savedPlaces.value = updated
        }
        Prefs.activePlace = place
        _activePlace.value = place
    }

    /** Remove a saved place; if it was active, fall back to the first remaining, else the default. */
    fun remove(place: Place) {
        val updated = _savedPlaces.value.filterNot { it.sameAs(place) }
        Prefs.savedPlaces = updated
        _savedPlaces.value = updated
        if (_activePlace.value.sameAs(place)) {
            val next = updated.firstOrNull()
            Prefs.activePlace = next                       // null → next read resolves to MiqatDefaults.place
            _activePlace.value = next ?: MiqatDefaults.place
        }
    }

    /** Same place regardless of coord precision. */
    private fun Place.sameAs(other: Place) = name == other.name && countryCode == other.countryCode
}
