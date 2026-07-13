package com.example.miqatapp.core.datetime

import com.example.miqatapp.core.debug.Debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlin.time.Duration.Companion.milliseconds

/** App clock: date, time and Hijri from one place. Debug can pin a date or run a fast clock. */
object Now {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val startDate = Debug.DATE_OVERRIDE ?: currentDate()

    private val _now = MutableStateFlow(
        if (Debug.FAST_CLOCK) LocalDateTime(startDate, LocalTime(0, 0)) else LocalDateTime(startDate, currentTime()),
    )
    /** The clock. Collect this; it advances on its own. */
    val now: StateFlow<LocalDateTime> = _now.asStateFlow()

    init {
        scope.launch {
            if (Debug.FAST_CLOCK) {
                // +5 virtual minutes per tick, rolls through all days
                var m = 0
                while (true) {
                    _now.value = LocalDateTime(startDate.plus(m / 1440, DateTimeUnit.DAY), LocalTime((m % 1440) / 60, (m % 1440) % 60))
                    m += 5
                    delay(80.milliseconds)
                }
            } else {
                while (true) {
                    _now.value = LocalDateTime(Debug.DATE_OVERRIDE ?: currentDate(), currentTime())
                    delay(1_000.milliseconds) // per-second so the countdown ticks and prayers switch on time
                }
            }
        }
    }

    /** Today (or the pushed/running debug date). */
    fun date(): LocalDate = _now.value.date

    /** Wall-clock time now. */
    fun time(): LocalTime = _now.value.time

    /** Today's Hijri date with [offsetDays] applied. */
    fun hijri(offsetDays: Int = 0): HijriDate = toHijri(date().plus(offsetDays, DateTimeUnit.DAY))
}
