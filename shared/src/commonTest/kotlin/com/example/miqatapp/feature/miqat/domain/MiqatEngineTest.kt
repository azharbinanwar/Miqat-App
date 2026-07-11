package com.example.miqatapp.feature.miqat.domain

import com.example.miqatapp.core.constants.defaults.MiqatDefaults
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Miqat
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MiqatEngineTest {

    private val calc = MiqatCalculation(
        method = CalculationMethod.MWL,
        madhab = Madhab.Shafi,
        highLatRule = HighLatRule.MiddleNight,
        fajrAngle = 18,
        ishaAngle = 17,
        adjustments = emptyMap(),
    )

    @Test
    fun makkah_times_are_ordered_and_plausible() {
        val times = MiqatEngine.timesFor(LocalDate(2024, 6, 21), MiqatDefaults.fallbackPlace, calc)
        val at = times.associate { it.miqat to it.at }

        assertEquals(12, times.size, "all Miqat points returned")

        // must run chronologically through the day
        val order = listOf(Miqat.Fajr, Miqat.Sunrise, Miqat.Dhuhr, Miqat.Asr, Miqat.Sunset, Miqat.Isha)
        for (i in 1 until order.size) {
            assertTrue(at.getValue(order[i - 1]) < at.getValue(order[i]), "${order[i - 1]} must precede ${order[i]}")
        }

        // Makkah solar noon lands around 12:2x local — catches sign / timezone-conversion errors
        assertEquals(12, at.getValue(Miqat.Dhuhr).hour, "Dhuhr should be just past local noon")
    }
}
