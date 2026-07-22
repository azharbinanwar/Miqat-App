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
import com.example.miqatapp.core.components.AppDrawer
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.components.LocalOverlay
import com.example.miqatapp.core.components.OverlayState
import com.example.miqatapp.feature.home.presentation.HomeAltScreen
import com.example.miqatapp.feature.onboarding.presentation.OnboardingScreen
import com.example.miqatapp.feature.home.presentation.HomeScreen
import com.example.miqatapp.feature.azkar.presentation.AzkarScreen
import com.example.miqatapp.feature.duas.presentation.DuasScreen
import com.example.miqatapp.feature.hijri.presentation.HijriCalendarScreen
import com.example.miqatapp.feature.home.presentation.PrayerAnimationScreen
import com.example.miqatapp.feature.miqat.presentation.MiqatTimesScreen
import com.example.miqatapp.feature.qibla.presentation.QiblaScreen
import com.example.miqatapp.feature.quran.presentation.QuranIndexScreen
import com.example.miqatapp.feature.quran.presentation.QuranReaderScreen
import com.example.miqatapp.feature.quran.presentation.QuranThemeHost
import com.example.miqatapp.feature.settings.presentation.LocationScreen
import com.example.miqatapp.feature.notifications.presentation.NotificationsScreen
import com.example.miqatapp.feature.settings.presentation.MiqatCalculationScreen
import com.example.miqatapp.feature.focus.presentation.PrayerFocusScreen
import com.example.miqatapp.feature.settings.presentation.SettingsScreen
import com.example.miqatapp.feature.settings.presentation.WidgetGalleryScreen
import com.example.miqatapp.feature.tasbih.presentation.TasbihRun
import com.example.miqatapp.feature.tasbih.presentation.TasbihScreen
import com.example.miqatapp.feature.tasbih.presentation.TasbihHistoryScreen
import com.example.miqatapp.feature.tasbih.presentation.TasbihHubScreen
import com.example.miqatapp.feature.tracker.presentation.TrackerScreen
import com.example.miqatapp.feature.sandbox.presentation.SandboxScreen


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
            NavHost(navController = navController, startDestination = AppRoute.Quran) {
                composable<AppRoute.Onboarding> { OnboardingScreen() }
                composable<AppRoute.Home> { HomeScreen() }
                composable<AppRoute.Sandbox> { SandboxScreen() }
                composable<AppRoute.PrayerTimes> { MiqatTimesScreen() }
                composable<AppRoute.Qibla> { QiblaScreen() }
                composable<AppRoute.Tracker> { TrackerScreen() }
                composable<AppRoute.Settings> {
                    SettingsScreen(
                        onNotifications = { navController.navigate(AppRoute.Notifications) },
                        onPrayerFocus = { navController.navigate(AppRoute.PrayerFocus) },
                        onPrayerCalc = { navController.navigate(AppRoute.MiqatCalculation) },
                        onLocation = { navController.navigate(AppRoute.Location) },
                        onWidgets = { navController.navigate(AppRoute.Widgets) },
                    )
                }
                composable<AppRoute.Widgets> { WidgetGalleryScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.Notifications> { NotificationsScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.PrayerFocus> { PrayerFocusScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.MiqatCalculation> { MiqatCalculationScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.Location> { LocationScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.HijriCalendar> { HijriCalendarScreen(onBack = { navController.popBackStack() }) }
                composable<AppRoute.Duas> { DuasScreen() }
                composable<AppRoute.Azkar> { AzkarScreen() }
                composable<AppRoute.Quran> {
                    QuranThemeHost { QuranIndexScreen(onOpen = { startId -> navController.navigate(AppRoute.QuranReader(startId)) }) }
                }
                composable<AppRoute.QuranReader> { entry ->
                    QuranThemeHost { QuranReaderScreen(startId = entry.toRoute<AppRoute.QuranReader>().startId, onBack = { navController.popBackStack() }) }
                }
                composable<AppRoute.Tasbih> { TasbihHubScreen(onHistory = { navController.navigate(AppRoute.TasbihHistory()) }) }
                composable<AppRoute.TasbihCounter> {
                    TasbihScreen(
                        // nav args can't carry object lists — the handoff global is read ONCE here,
                        // at the nav boundary, and the screen itself stays a pure function of its inputs
                        zikrs = TasbihRun.queue,
                        onBack = { navController.popBackStack() },
                        onHistory = { id -> navController.navigate(AppRoute.TasbihHistory(id)) },
                    )
                }
                composable<AppRoute.TasbihHistory> { entry ->
                    TasbihHistoryScreen(dhikrId = entry.toRoute<AppRoute.TasbihHistory>().dhikrId, onBack = { navController.popBackStack() })
                }
                composable<AppRoute.HomeAlt> { HomeAltScreen() }
                composable<AppRoute.PrayerAnimation> { PrayerAnimationScreen() }
            }
        }
    }
}
