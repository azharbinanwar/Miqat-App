package com.example.miqatapp.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

/**
 * Navigation accessible from non-UI code (e.g. ViewModels later, via DI).
 * Wraps the same NavController the UI uses — single source of truth.
 */
interface AppNavigator {
    fun navigate(route: AppRoute)
    fun back()
}

internal class AppNavigatorImpl(private val nav: NavHostController) : AppNavigator {
    override fun navigate(route: AppRoute) { nav.navigate(route) }
    override fun back() { nav.popBackStack() }
}

/** UI access (auto_route's `context.router` equivalent) — read in any composable. */
val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("LocalNavController not set — wrap content in AppNavHost.")
}

/** Same navigator the UI uses, for code that prefers the interface. */
val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> {
    error("LocalAppNavigator not set — wrap content in AppNavHost.")
}
