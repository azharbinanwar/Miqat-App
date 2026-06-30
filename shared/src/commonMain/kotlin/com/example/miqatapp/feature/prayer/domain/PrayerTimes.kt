package com.example.miqatapp.feature.prayer.domain

import com.example.miqatapp.core.enums.Prayer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** One day's prayer times, in local wall-clock time for the place's timezone. */
data class PrayerTimes(
    val date: LocalDate,
    val times: Map<Prayer, LocalTime>,
)
