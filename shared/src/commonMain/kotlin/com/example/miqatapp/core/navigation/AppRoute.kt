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
    data object Onboarding : AppRoute

    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Sandbox : AppRoute

    @Serializable
    data object PrayerTimes : AppRoute

    @Serializable
    data object Qibla : AppRoute

    @Serializable
    data object Tracker : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object Notifications : AppRoute

    @Serializable
    data object PrayerFocus : AppRoute

    @Serializable
    data object PrayerCalculation : AppRoute

    @Serializable
    data object Location : AppRoute

    @Serializable
    data object HijriCalendar : AppRoute

    @Serializable
    data object Tasbih : AppRoute

    @Serializable
    data object TasbihCounter : AppRoute

    @Serializable
    data class TasbihHistory(val dhikrId: String? = null) : AppRoute

    @Serializable
    data object HomeAlt : AppRoute

    @Serializable
    data object PrayerAnimation : AppRoute

    @Serializable
    data object MosqueScene : AppRoute
}
