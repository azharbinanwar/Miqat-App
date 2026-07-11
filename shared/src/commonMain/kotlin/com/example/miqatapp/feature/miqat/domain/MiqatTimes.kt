package com.example.miqatapp.feature.miqat.domain

import com.example.miqatapp.core.enums.Miqat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** One day's prayer times, in local wall-clock time for the place's timezone. */
data class MiqatTimes(
    val date: LocalDate,
    val times: Map<Miqat, LocalTime>,
)
