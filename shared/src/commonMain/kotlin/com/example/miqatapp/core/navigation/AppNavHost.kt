package com.example.miqatapp.core.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.miqatapp.core.widgets.AppDrawer
import com.example.miqatapp.core.widgets.LocalDrawerState
import com.example.miqatapp.core.widgets.LocalOverlay
import com.example.miqatapp.core.widgets.OverlayState
import com.example.miqatapp.feature.home.presentation.HomeAltScreen
import com.example.miqatapp.feature.onboarding.presentation.OnboardingScreen
import com.example.miqatapp.feature.home.presentation.HomeScreen
import com.example.miqatapp.feature.home.presentation.MosqueSceneScreen
import com.example.miqatapp.feature.home.presentation.PrayerAnimationScreen
import com.example.miqatapp.feature.prayer.presentation.PrayerTimesScreen
import com.example.miqatapp.feature.qibla.presentation.QiblaScreen
import com.example.miqatapp.feature.settings.presentation.LocationScreen
import com.example.miqatapp.feature.settings.presentation.NotificationsScreen
import com.example.miqatapp.feature.settings.presentation.PrayerCalculationScreen
import com.example.miqatapp.feature.settings.presentation.PrayerFocusScreen
import com.example.miqatapp.feature.settings.presentation.SettingsScreen
import com.example.miqatapp.feature.tasbih.presentation.TasbihScreen
import com.example.miqatapp.feature.tasbih.presentation.TasbihHistoryScreen
import com.example.miqatapp.feature.tasbih.presentation.TasbihHubScreen
import com.example.miqatapp.feature.tracker.presentation.TrackerScreen
import com.example.miqatapp.feature.sandbox.presentation.SandboxScreen

/**
 * App-wide navigation host. Sets up the NavController, exposes it both ways
 * (LocalNavController for UI, LocalAppNavigator for the interface), and lists routes.
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val navigator = remember(navController) { AppNavigatorImpl(navController) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val overlay = remember { OverlayState() }

    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalAppNavigator provides navigator,
        LocalDrawerState provides drawerState,
        LocalOverlay provides overlay,
    ) {
        // One drawer around the whole NavHost — navigation only swaps content, never rebuilds it.
        AppDrawer(drawerState) {
            NavHost(navController = navController, startDestination = AppRoute.Home) {
                composable<AppRoute.Onboarding> { OnboardingScreen() }
                composable<AppRoute.Home> { HomeScreen() }
                composable<AppRoute.Sandbox> { SandboxScreen() }
                composable<AppRoute.PrayerTimes> { PrayerTimesScreen() }
                composable<AppRoute.Qibla> { QiblaScreen() }
                composable<AppRoute.Tracker> { TrackerScreen() }
                composable<AppRoute.Settings> {
                    SettingsScreen(
                        onNotifications = { navController.navigate(AppRoute.Notifications) },
                        onPrayerFocus = { navController.navigate(AppRoute.PrayerFocus) },
                        onPrayerCalc = { navController.navigate(AppRoute.PrayerCalculation) },
                        onLocation = { navController.navigate(AppRoute.Location) },
                    )
                }
                composable<AppRoute.Notifications> { NotificationsScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.PrayerFocus> { PrayerFocusScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.PrayerCalculation> { PrayerCalculationScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.Location> { LocationScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.Tasbih> { TasbihHubScreen(onHistory = { navController.navigate(AppRoute.TasbihHistory()) }) }
                composable<AppRoute.TasbihCounter> {
                    TasbihScreen(
                        onBack = { navController.popBackStack() },
                        onHistory = { id -> navController.navigate(AppRoute.TasbihHistory(id)) },
                    )
                }
                composable<AppRoute.TasbihHistory> { entry ->
                    TasbihHistoryScreen(dhikrId = entry.toRoute<AppRoute.TasbihHistory>().dhikrId, onBack = { navController.popBackStack() })
                }
                composable<AppRoute.HomeAlt> { HomeAltScreen() }
                composable<AppRoute.PrayerAnimation> { PrayerAnimationScreen() }
                composable<AppRoute.MosqueScene> { MosqueSceneScreen() }
            }
        }
    }
}
