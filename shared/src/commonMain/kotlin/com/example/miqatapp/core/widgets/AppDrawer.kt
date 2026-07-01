package com.example.miqatapp.core.widgets

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.draw.blur
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MoonStar
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.SquareCheck
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.navigation.AppRoute
import com.example.miqatapp.core.navigation.LocalNavController
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.about_miqat
import com.example.miqatapp.resources.app_name
import com.example.miqatapp.resources.developer_sandbox
import com.example.miqatapp.resources.duas_and_adhkar
import com.example.miqatapp.resources.hijri_calendar
import com.example.miqatapp.resources.home
import com.example.miqatapp.resources.miqat_logo
import com.example.miqatapp.resources.prayer_times
import com.example.miqatapp.resources.prayer_tracker
import com.example.miqatapp.resources.qibla_compass
import com.example.miqatapp.resources.settings
import com.example.miqatapp.resources.tasbih_counter
import com.example.miqatapp.resources.your_daily_prayer_companion
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** One drawer row. [route] null = not built yet (no-op for now). */
private data class DrawerEntry(val label: StringResource, val icon: ImageVector, val route: AppRoute?)

private val drawerItems = listOf(
    DrawerEntry(Res.string.home, Lucide.House, AppRoute.Home),
    DrawerEntry(Res.string.prayer_times, Lucide.Clock, AppRoute.PrayerTimes),
    DrawerEntry(Res.string.qibla_compass, Lucide.Compass, AppRoute.Qibla),
    DrawerEntry(Res.string.prayer_tracker, Lucide.SquareCheck, AppRoute.Tracker),
    DrawerEntry(Res.string.hijri_calendar, Lucide.Calendar, null),
    DrawerEntry(Res.string.duas_and_adhkar, Lucide.BookOpen, null),
    DrawerEntry(Res.string.tasbih_counter, Lucide.MoonStar, AppRoute.Tasbih),
)

private val footerItems = listOf(
    DrawerEntry(Res.string.settings, Lucide.Settings, AppRoute.Settings),
    DrawerEntry(Res.string.developer_sandbox, Lucide.Flame, AppRoute.Sandbox),
    DrawerEntry(Res.string.about_miqat, Lucide.Info, null),
)

/** Shared drawer state, hoisted at the nav host so navigating never rebuilds the drawer. */
val LocalDrawerState = staticCompositionLocalOf<DrawerState> { error("LocalDrawerState not provided") }

/** Counts open modal overlays (bottom sheets) so the drawer can blur the app behind them too. */
class OverlayState {
    var sheetCount by mutableStateOf(0)
    var drawerGesturesEnabled by mutableStateOf(true) // a screen can switch off edge-swipe-to-open
}
val LocalOverlay = staticCompositionLocalOf<OverlayState> { error("LocalOverlay not provided") }

/**
 * App-wide side navigation drawer. Hoisted once around the NavHost; open via [drawerState].
 * Items with no route yet are still shown but do nothing (built when their screen lands).
 */
@Composable
fun AppDrawer(
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()
    val overlay = LocalOverlay.current
    val blurred = drawerState.targetValue == DrawerValue.Open || overlay.sheetCount > 0
    val blurRadius by animateDpAsState(if (blurred) 18.dp else 0.dp, label = "blur")

    // Top-level destinations: replace instead of stacking. Back always returns to Home.
    val onSelect: (DrawerEntry) -> Unit = { entry ->
        scope.launch { drawerState.close() }
        val route = entry.route
        if (route != null && nav.currentDestination?.hasRoute(route::class) != true) {
            nav.navigate(route) {
                popUpTo(AppRoute.Home) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        gesturesEnabled = drawerState.targetValue == DrawerValue.Open || overlay.drawerGesturesEnabled,
        scrimColor = Color.Black.copy(alpha = 0.32f), // real dim so the panel floats (matches AppBottomSheet)
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = AppTheme.colors.surfaceContainerHigh, // distinct elevated panel, not raw white-on-white
                drawerShape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.systemBars) // stay clear of notch / nav bar
                    .padding(12.dp) // uniform floating gap, identical on every side
                    .width(296.dp),
            ) {
                DrawerHeader()
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(Modifier.verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
                    drawerItems.forEach { entry ->
                        DrawerRow(entry) { onSelect(entry) }
                    }
                    HorizontalDivider(
                        Modifier.padding(horizontal = 28.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    footerItems.forEach { entry ->
                        DrawerRow(entry) { onSelect(entry) }
                    }
                }
            }
        },
        content = { Box(Modifier.fillMaxSize().blur(blurRadius)) { content() } },
    )
}

@Composable
private fun DrawerHeader() {
    Row(
        Modifier.fillMaxWidth().padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(48.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(Res.drawable.miqat_logo),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer),
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                stringResource(Res.string.your_daily_prayer_companion),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DrawerRow(entry: DrawerEntry, onClick: () -> Unit) {
    val backEntry by LocalNavController.current.currentBackStackEntryAsState()
    val route = entry.route
    val selected = route != null && backEntry?.destination?.hasRoute(route::class) == true
    NavigationDrawerItem(
        label = { Text(stringResource(entry.label)) },
        icon = { Icon(entry.icon, null, modifier = Modifier.size(22.dp)) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding).height(52.dp),
    )
}
