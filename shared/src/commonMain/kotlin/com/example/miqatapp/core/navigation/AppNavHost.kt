package com.example.miqatapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miqatapp.feature.home.presentation.HomeAltScreen
import com.example.miqatapp.feature.home.presentation.HomeScreen
import com.example.miqatapp.feature.home.presentation.MosqueSceneScreen
import com.example.miqatapp.feature.home.presentation.PrayerAnimationScreen
import com.example.miqatapp.feature.sandbox.presentation.SandboxScreen

/**
 * App-wide navigation host. Sets up the NavController, exposes it both ways
 * (LocalNavController for UI, LocalAppNavigator for the interface), and lists routes.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navigator = remember(navController) { AppNavigatorImpl(navController) }

    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalAppNavigator provides navigator,
    ) {
        NavHost(navController = navController, startDestination = AppRoute.Home) {
            composable<AppRoute.Home> { HomeScreen() }
            composable<AppRoute.Sandbox> { SandboxScreen() }
            composable<AppRoute.HomeAlt> { HomeAltScreen() }
            composable<AppRoute.PrayerAnimation> { PrayerAnimationScreen() }
            composable<AppRoute.MosqueScene> { MosqueSceneScreen() }
        }
    }
}
