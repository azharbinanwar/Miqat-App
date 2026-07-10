package com.example.miqatapp.core.permissions

import androidx.compose.runtime.Composable

/** Runtime permissions the app requests. Add an entry only when a feature actually needs it. */
enum class AppPermission { Location, Notifications }

/** Outcome of a status check or a request. */
enum class PermissionStatus {
    Granted,
    Denied,             // refused, but we may ask again
    DeniedPermanently,  // refused with "don't ask again" (Android) / denied (iOS) — must route to Settings
    NotDetermined,      // never asked
}

/**
 * One shared entry point for runtime permissions across Android and iOS. Screens depend on this
 * interface, never on platform APIs — so the whole app requests permissions the same way.
 *
 * Obtain it with [rememberPermissionService] inside a composable: Android needs the Activity + result
 * launcher from the Compose tree, so this can't be a context-free singleton.
 */
interface PermissionService {
    /** Current status without prompting. */
    suspend fun status(permission: AppPermission): PermissionStatus

    /** Prompt if needed; returns the resulting status. Safe to call when already granted (returns Granted). */
    suspend fun request(permission: AppPermission): PermissionStatus

    /** Open the OS app-settings page — the only recourse once a permission is DeniedPermanently. */
    fun openAppSettings()
}

/** Platform-backed [PermissionService], bound to the current Compose context. */
@Composable
expect fun rememberPermissionService(): PermissionService
