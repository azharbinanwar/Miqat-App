package com.example.miqatapp.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe routes — the auto_route equivalent. Each destination is a
 * `@Serializable` object/class; args become constructor params.
 *
 * Add a screen: declare it here, register it in [AppNavHost], navigate with the object.
 *   data class PrayerDetail(val date: String) : AppRoute   // example with args
 */
sealed interface AppRoute {
    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Sandbox : AppRoute
}
