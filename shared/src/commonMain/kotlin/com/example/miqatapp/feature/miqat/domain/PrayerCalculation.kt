package com.example.miqatapp.feature.miqat.domain

import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Miqat

/**
 * A frozen set of prayer-calculation settings — the engine's input (a plain DTO: data only, no state,
 * no logic). Built by `MiqatCalculationStore.snapshot()`; the engine takes this one object instead of
 * six loose parameters, and stays a pure function with no dependency on the store.
 */
data class MiqatCalculation(
    val method: CalculationMethod,
    val madhab: Madhab,
    val highLatRule: HighLatRule,
    val fajrAngle: Int,
    val ishaAngle: Int,
    val adjustments: Map<Miqat, Int>,
)
