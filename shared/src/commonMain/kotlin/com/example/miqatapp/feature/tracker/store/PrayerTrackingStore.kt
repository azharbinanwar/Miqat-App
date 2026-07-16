package com.example.miqatapp.feature.tracker.store

import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.PrayerTrackerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Today's prayer tracking. In memory for now; Room-backed later. Screens read [tracked]. */
object PrayerTrackingStore {
    private val _tracked = MutableStateFlow<Map<Miqat, PrayerTrackerStatus>>(emptyMap())
    val tracked: StateFlow<Map<Miqat, PrayerTrackerStatus>> = _tracked.asStateFlow()

    fun setStatus(prayer: Miqat, status: PrayerTrackerStatus?) {
        _tracked.value = if (status == null) _tracked.value - prayer else _tracked.value + (prayer to status)
    }
}
