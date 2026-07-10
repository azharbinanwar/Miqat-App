package com.example.miqatapp.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

@Composable
actual fun rememberGeoLocator(): GeoLocator = remember { IosGeoLocator() }

private class IosGeoLocator : GeoLocator {
    private val manager = CLLocationManager()
    private var delegate: LocationDelegate? = null // strong ref so it survives until the callback fires

    override suspend fun current(): Coordinates? = suspendCoroutine { cont ->
        val d = LocationDelegate(cont)
        delegate = d
        manager.delegate = d
        manager.requestLocation() // one-shot; delivered via the delegate below
    }
}

private class LocationDelegate(
    private val cont: Continuation<Coordinates?>,
) : NSObject(), CLLocationManagerDelegateProtocol {
    private var resumed = false

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        if (resumed) return
        resumed = true
        val loc = didUpdateLocations.lastOrNull() as? CLLocation
        cont.resume(loc?.coordinate?.useContents { Coordinates(latitude, longitude) })
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        if (resumed) return
        resumed = true
        cont.resume(null)
    }
}
