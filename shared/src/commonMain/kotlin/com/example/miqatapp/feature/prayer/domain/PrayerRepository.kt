package com.example.miqatapp.feature.prayer.domain

import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.Madhab
import kotlinx.datetime.LocalDate

/** Computes prayer times. Impl uses Adhan; UI/ViewModel only see this. */
interface PrayerRepository {
    fun timesFor(
        date: LocalDate,
        place: Place,
        method: CalculationMethod,
        madhab: Madhab,
    ): PrayerTimes
}
