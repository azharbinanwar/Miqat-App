package com.example.miqatapp.feature.miqat.store

import com.example.miqatapp.core.datetime.Now
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.feature.miqat.domain.MiqatEngine
import com.example.miqatapp.feature.miqat.domain.MiqatTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

/**
 * Holds computed Miqat times — the only caller of the engine, so the UI reads this store, never the engine.
 * `today` recomputes whenever the settings, the active place, or the [Now] date changes — so it follows
 * a pushed or fast debug clock for free, and rolls over at midnight on its own. `timesFor` computes any other
 * day on demand (calendar).
 */
object MiqatTimesStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val today: StateFlow<List<MiqatTime>> = combine(
        MiqatCalculationStore.calculation,
        LocationStore.activePlace,
        Now.now.map { it.date }.distinctUntilChanged(),
    ) { calc, place, date ->
        MiqatEngine.timesFor(date, place, calc)
    }.stateIn(scope, SharingStarted.Eagerly, compute(Now.date()))

    /** Any day on demand (calendar browsing), using the current place + settings. Holds nothing. */
    fun timesFor(date: LocalDate): List<MiqatTime> = compute(date)

    private fun compute(date: LocalDate) =
        MiqatEngine.timesFor(date, LocationStore.activePlace.value, MiqatCalculationStore.calculation.value)
}
