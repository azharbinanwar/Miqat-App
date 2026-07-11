package com.example.miqatapp.feature.miqat.domain

import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.enums.HighLatRule
import com.example.miqatapp.core.enums.Madhab
import com.example.miqatapp.core.enums.Miqat

/** Frozen calc settings — the engine's input. Plain data, no logic. */
data class MiqatCalculation(
    val method: CalculationMethod,
    val madhab: Madhab,
    val highLatRule: HighLatRule,
    val fajrAngle: Int,
    val ishaAngle: Int,
    val adjustments: Map<Miqat, Int>,
)
