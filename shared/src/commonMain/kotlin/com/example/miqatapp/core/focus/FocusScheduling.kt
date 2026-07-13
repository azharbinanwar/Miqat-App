package com.example.miqatapp.core.focus

import com.example.miqatapp.core.store.PrayerFocusStore
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

// Re-arms the prayer alarms whenever the times (date/method/location) or the focus settings change.
// Reuses MiqatTimesStore.today — the same reactive source the home screen already recomputes from.
object FocusScheduling {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    // Call once at app start, after AppCtx is set.
    fun start() {
        if (started) return
        started = true
        scope.launch {
            combine(MiqatTimesStore.today, PrayerFocusStore.configs) { times, configs -> times to configs }
                .distinctUntilChanged()
                .drop(1) // the initial value; app start already armed once
                .collect { PhoneSilencer.rescheduleAll() }
        }
    }
}
