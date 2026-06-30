package com.example.miqatapp.feature.qibla.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun rememberHeading(): HeadingState {
    val available = remember { CLLocationManager.headingAvailable() }
    var state by remember { mutableStateOf(HeadingState(available = available)) }

    DisposableEffect(available) {
        if (!available) {
            state = HeadingState(available = false)
            return@DisposableEffect onDispose { }
        }
        val manager = CLLocationManager()
        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
                // trueHeading is -1 when unavailable; fall back to magneticHeading.
                val h = didUpdateHeading.trueHeading.takeIf { it >= 0 } ?: didUpdateHeading.magneticHeading
                val accurate = didUpdateHeading.headingAccuracy in 0.0..20.0
                state = state.copy(degrees = h.toFloat(), available = true, accurate = accurate)
            }
        }
        manager.delegate = delegate
        manager.startUpdatingHeading()
        onDispose {
            manager.stopUpdatingHeading()
            manager.delegate = null
        }
    }
    return state
}
