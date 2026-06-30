package com.example.miqatapp.feature.qibla.presentation

import androidx.compose.runtime.Composable

/**
 * Device heading.
 * @param degrees compass heading 0..360 (clockwise from magnetic/true north).
 * @param available device has a usable compass sensor.
 * @param accurate sensor reports a trustworthy reading (else show calibration hint).
 */
data class HeadingState(
    val degrees: Float = 0f,
    val available: Boolean = true,
    val accurate: Boolean = true,
)

/** Platform compass. Android: rotation-vector sensor. iOS: CLLocationManager heading. */
@Composable
expect fun rememberHeading(): HeadingState
