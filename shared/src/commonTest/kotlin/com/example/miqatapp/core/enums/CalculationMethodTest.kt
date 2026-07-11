package com.example.miqatapp.core.enums

import kotlin.test.Test
import kotlin.test.assertEquals

class CalculationMethodTest {

    @Test
    fun each_country_maps_to_exactly_one_method() {
        val all = CalculationMethod.entries.flatMap { it.countries }
        assertEquals(all.size, all.toSet().size, "a country code appears in more than one method")
    }

    @Test
    fun forCountry_resolves_and_falls_back_to_mwl() {
        assertEquals(CalculationMethod.Karachi, CalculationMethod.forCountry("PK"))
        assertEquals(CalculationMethod.Karachi, CalculationMethod.forCountry("pk"))   // case-insensitive
        assertEquals(CalculationMethod.Makkah, CalculationMethod.forCountry("SA"))
        assertEquals(CalculationMethod.MWL, CalculationMethod.forCountry("ZZ"))        // unmapped
        assertEquals(CalculationMethod.MWL, CalculationMethod.forCountry(null))
    }
}
