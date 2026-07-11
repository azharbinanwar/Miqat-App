package com.example.miqatapp.feature.qibla.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Palette
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.components.StateView
import com.example.miqatapp.feature.qibla.domain.distanceToMakkahKm
import com.example.miqatapp.feature.qibla.domain.qiblaBearing
import com.example.miqatapp.feature.qibla.presentation.components.CompassCalibration
import com.example.miqatapp.feature.qibla.presentation.components.QiblaDialFor
import com.example.miqatapp.feature.qibla.presentation.components.QiblaStyleSheet
import com.example.miqatapp.feature.qibla.store.QiblaStyleStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.compass_unavailable
import com.example.miqatapp.resources.device_has_no_compass_sensor
import com.example.miqatapp.resources.qibla
import com.example.miqatapp.resources.turn_left_to_face_qibla
import com.example.miqatapp.resources.turn_right_to_face_qibla
import com.example.miqatapp.resources.you_are_facing_qibla
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.roundToInt

private const val ALIGN_TOLERANCE_DEG = 5f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen() {
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    val place by LocationStore.activePlace.collectAsState()
    val qiblaDeg = qiblaBearing(place.latitude, place.longitude).toFloat()
    val distance = distanceToMakkahKm(place.latitude, place.longitude)

    val heading = rememberHeading()
    val qiblaStyle by QiblaStyleStore.style.collectAsState()

    var calibrationDismissed by remember { mutableStateOf(false) }
    var showStyleSheet by remember { mutableStateOf(false) }
    val needsCalibration = !heading.accurate

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.qibla)) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") }
                },
                actions = {
                    IconButton(onClick = { showStyleSheet = true }) { Icon(Lucide.Palette, "Compass style") }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            if (!heading.available) {
                StateView(
                    title = stringResource(Res.string.compass_unavailable),
                    message = stringResource(Res.string.device_has_no_compass_sensor),
                    icon = { Icon(Lucide.Compass, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(56.dp)) },
                )
                return@Box
            }

            // angle from current facing to qibla, normalized to -180..180
            val delta = (((qiblaDeg - heading.degrees) + 540f) % 360f) - 180f
            val aligned = abs(delta) <= ALIGN_TOLERANCE_DEG

            Column(
                Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp),
            ) {
                QiblaDialFor(qiblaStyle, heading.degrees, qiblaDeg, aligned)

                // the reading — the hero
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${qiblaDeg.roundToInt()}°",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.onSurface,
                    )
                    val hint = when {
                        aligned -> stringResource(Res.string.you_are_facing_qibla)
                        delta > 0 -> stringResource(Res.string.turn_right_to_face_qibla)
                        else -> stringResource(Res.string.turn_left_to_face_qibla)
                    }
                    Text(
                        hint,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (aligned) AppTheme.colors.success else AppTheme.colors.primary,
                    )
                }

                // supporting info in the app's card style. ponytail: labels inline; move to resources with the rest.
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(AppTheme.colors.cardColor).height(IntrinsicSize.Min),
                ) {
                    InfoCell("Direction", "${qiblaDeg.roundToInt()}° ${cardinal(qiblaDeg)}", Modifier.weight(1f))
                    VerticalDivider(color = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.15f))
                    InfoCell("Distance to Makkah", "${distance.roundToInt()} km", Modifier.weight(1f))
                }
            }

            if (needsCalibration && !calibrationDismissed) {
                AppBottomSheet(
                    onDismiss = { calibrationDismissed = true },
                    title = "Calibrate compass", // ponytail: inline copy like the rest of this screen; move to resources with them
                ) {
                    CompassCalibration(Modifier.padding(vertical = 8.dp))
                }
            }

            if (showStyleSheet) {
                QiblaStyleSheet(
                    current = qiblaStyle,
                    headingDeg = heading.degrees,
                    qiblaDeg = qiblaDeg,
                    aligned = aligned,
                    onSelect = { QiblaStyleStore.setStyle(it) },
                    onDismiss = { showStyleSheet = false },
                )
            }
        }
    }
}

@Composable
private fun InfoCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppTheme.colors.onSurface)
    }
}

/** 8-point cardinal for a bearing, e.g. 267° → "W". */
private fun cardinal(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
    return dirs[(((deg % 360f + 360f) % 360f) / 45f).roundToInt() % 8]
}
