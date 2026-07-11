package com.example.miqatapp.feature.miqat.store

import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.feature.miqat.domain.MiqatEngine
import com.example.miqatapp.feature.miqat.domain.MiqatTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate

/**
 * Holds computed Miqat times — the only caller of the engine, so the UI reads this store, never the engine.
 * `today` stays hot for Home and notifications; `timesFor` computes any other day on demand (calendar).
 */
object MiqatTimesStore {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _date = MutableStateFlow(currentDate())

    /** Recomputes whenever the settings, the active place, or the day changes. */
    val today: StateFlow<List<MiqatTime>> = combine(
        MiqatCalculationStore.calculation,
        LocationStore.activePlace,
        _date,
    ) { calc, place, date ->
        MiqatEngine.timesFor(date, place, calc)
    }.stateIn(scope, SharingStarted.Eagerly, compute(_date.value))

    /** Any day on demand (calendar browsing), using the current place + settings. Holds nothing. */
    fun timesFor(date: LocalDate): List<MiqatTime> = compute(date)

    /** Roll `today` past midnight — call on app resume. */
    fun refreshDate() { _date.value = currentDate() }

    private fun compute(date: LocalDate) =
        MiqatEngine.timesFor(date, LocationStore.activePlace.value, MiqatCalculationStore.calculation.value)
}
