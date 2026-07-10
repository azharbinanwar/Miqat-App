package com.example.miqatapp.core.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusDenied
import platform.UserNotifications.UNAuthorizationStatusNotDetermined
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNUserNotificationCenter

@Composable
actual fun rememberPermissionService(): PermissionService = remember { IosPermissionService() }

private class IosPermissionService : PermissionService {
    private val locationManager = CLLocationManager()
    private var authDelegate: LocationAuthDelegate? = null // strong ref until the callback fires

    override suspend fun status(permission: AppPermission): PermissionStatus = when (permission) {
        AppPermission.Location -> locationManager.authorizationStatus.toStatus()
        AppPermission.Notifications -> notificationStatus()
    }

    override suspend fun request(permission: AppPermission): PermissionStatus = when (permission) {
        AppPermission.Location -> {
            val current = locationManager.authorizationStatus
            if (current != kCLAuthorizationStatusNotDetermined) {
                current.toStatus()
            } else {
                // Prompt, then await the delegate's authorization-change callback for the real result.
                suspendCoroutine { cont ->
                    val d = LocationAuthDelegate(cont)
                    authDelegate = d
                    locationManager.delegate = d
                    locationManager.requestWhenInUseAuthorization()
                }
            }
        }
        AppPermission.Notifications -> {
            val granted = suspendCoroutine<Boolean> { cont ->
                UNUserNotificationCenter.currentNotificationCenter().requestAuthorizationWithOptions(
                    UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
                ) { granted, _ -> cont.resume(granted) }
            }
            if (granted) PermissionStatus.Granted else PermissionStatus.DeniedPermanently
        }
    }

    override fun openAppSettings() {
        val url = NSURL(string = UIApplicationOpenSettingsURLString)
        // Modern API — the deprecated openURL(_:) force-returns NO on current iOS and never opens Settings.
        UIApplication.sharedApplication.openURL(url, options = emptyMap<Any?, Any>(), completionHandler = null)
    }

    private suspend fun notificationStatus(): PermissionStatus = suspendCoroutine { cont ->
        UNUserNotificationCenter.currentNotificationCenter().getNotificationSettingsWithCompletionHandler { settings ->
            cont.resume(
                when (settings?.authorizationStatus) {
                    UNAuthorizationStatusAuthorized, UNAuthorizationStatusProvisional -> PermissionStatus.Granted
                    UNAuthorizationStatusDenied -> PermissionStatus.DeniedPermanently
                    UNAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
                    else -> PermissionStatus.NotDetermined
                },
            )
        }
    }
}

/** Resumes once when the user answers the location prompt (authorization changes off NotDetermined). */
private class LocationAuthDelegate(
    private val cont: Continuation<PermissionStatus>,
) : NSObject(), CLLocationManagerDelegateProtocol {
    private var resumed = false
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = manager.authorizationStatus
        if (resumed || status == kCLAuthorizationStatusNotDetermined) return
        resumed = true
        cont.resume(status.toStatus())
    }
}

private fun CLAuthorizationStatus.toStatus(): PermissionStatus = when (this) {
    kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> PermissionStatus.Granted
    kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> PermissionStatus.DeniedPermanently
    kCLAuthorizationStatusNotDetermined -> PermissionStatus.NotDetermined
    else -> PermissionStatus.NotDetermined
}
