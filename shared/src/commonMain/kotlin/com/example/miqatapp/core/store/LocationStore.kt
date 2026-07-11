package com.example.miqatapp.core.store

import com.example.miqatapp.core.constants.defaults.MiqatDefaults
import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Single source of truth for the user's location — the only `Prefs ?: MiqatDefaults` resolution for place.
 * Same shape as the other stores: seed each flow once, consumers observe the flow, setters persist + emit.
 *
 * This store owns the `Place` (de)serialization — [PrefsService] only stores/reads the raw JSON string,
 * it doesn't know what a Place is.
 */
object LocationStore {
    private val json = Json { ignoreUnknownKeys = true }

    private val _activePlace = MutableStateFlow(readActive() ?: MiqatDefaults.place)
    val activePlace: StateFlow<Place> = _activePlace.asStateFlow()

    private val _savedPlaces = MutableStateFlow(readSaved())
    val savedPlaces: StateFlow<List<Place>> = _savedPlaces.asStateFlow()

    /** Pick a place: add to saved (deduped, newest first) and make it active. */
    fun setActive(place: Place) {
        if (_savedPlaces.value.none { it.sameAs(place) }) {
            val updated = listOf(place) + _savedPlaces.value
            writeSaved(updated)
            _savedPlaces.value = updated
        }
        writeActive(place)
        _activePlace.value = place
    }

    /** Remove a saved place; if it was active, fall back to the first remaining, else the default. */
    fun remove(place: Place) {
        val updated = _savedPlaces.value.filterNot { it.sameAs(place) }
        writeSaved(updated)
        _savedPlaces.value = updated
        if (_activePlace.value.sameAs(place)) {
            val next = updated.firstOrNull()
            writeActive(next)                                     // null → next read resolves to MiqatDefaults.place
            _activePlace.value = next ?: MiqatDefaults.place
        }
    }

    // ── persistence: this store owns the Place JSON, PrefsService only holds the raw string ──

    private fun readActive(): Place? =
        PrefsService.getStringOrNull(PrefConst.ACTIVE_PLACE)?.let { runCatching { json.decodeFromString<Place>(it) }.getOrNull() }

    private fun readSaved(): List<Place> =
        PrefsService.getStringOrNull(PrefConst.SAVED_PLACES)?.let { runCatching { json.decodeFromString<List<Place>>(it) }.getOrNull() } ?: emptyList()

    private fun writeActive(place: Place?) {
        if (place == null) PrefsService.remove(PrefConst.ACTIVE_PLACE)
        else PrefsService.putString(PrefConst.ACTIVE_PLACE, json.encodeToString(place))
    }

    private fun writeSaved(list: List<Place>) {
        if (list.isEmpty()) PrefsService.remove(PrefConst.SAVED_PLACES)
        else PrefsService.putString(PrefConst.SAVED_PLACES, json.encodeToString(list))
    }

    /** Same place regardless of coord precision. */
    private fun Place.sameAs(other: Place) = name == other.name && countryCode == other.countryCode
}
