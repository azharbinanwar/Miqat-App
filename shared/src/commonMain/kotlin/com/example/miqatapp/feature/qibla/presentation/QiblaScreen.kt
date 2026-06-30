package com.example.miqatapp.feature.qibla.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.constants.Places
import com.example.miqatapp.core.widgets.LocalDrawerState
import com.example.miqatapp.core.widgets.StateView
import com.example.miqatapp.feature.qibla.domain.distanceToMakkahKm
import com.example.miqatapp.feature.qibla.domain.qiblaBearing
import com.example.miqatapp.feature.qibla.presentation.components.QiblaDial
import com.example.miqatapp.feature.qibla.presentation.components.QiblaDialClassical
import com.example.miqatapp.feature.qibla.presentation.components.QiblaDialModern
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.compass_accuracy_low_wave_phone_in_figure_eight
import com.example.miqatapp.resources.compass_unavailable
import com.example.miqatapp.resources.device_has_no_compass_sensor
import com.example.miqatapp.resources.distance_to_makkah_km
import com.example.miqatapp.resources.qibla
import com.example.miqatapp.resources.qibla_direction_degrees
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

    // ponytail: no Location module yet — use the default place (Makkah). Swap when Location lands.
    val place = Places.default
    val qiblaDeg = qiblaBearing(place.latitude, place.longitude).toFloat()
    val distance = distanceToMakkahKm(place.latitude, place.longitude)

    val heading = rememberHeading()
    var style by remember { mutableStateOf(QiblaStyle.Modern) }

    Scaffold(
            containerColor = AppTheme.colors.scaffoldBackgroundColor,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.qibla), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppTheme.colors.scaffoldBackgroundColor,
                        titleContentColor = AppTheme.colors.onSurface,
                        navigationIconContentColor = AppTheme.colors.onSurface,
                    ),
                )
            },
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                if (!heading.available) {
                    StateView(
                        title = stringResource(Res.string.compass_unavailable),
                        message = stringResource(Res.string.device_has_no_compass_sensor),
                        icon = {
                            Icon(
                                Lucide.Compass,
                                null,
                                tint = AppTheme.colors.onSurfaceVariant,
                                modifier = Modifier.size(56.dp),
                            )
                        },
                    )
                    return@Box
                }

                // angle from current facing to qibla, normalized to -180..180
                val delta = (((qiblaDeg - heading.degrees) + 540f) % 360f) - 180f
                val aligned = abs(delta) <= ALIGN_TOLERANCE_DEG

                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // style switcher
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QiblaStyle.entries.forEach { s ->
                            FilterChip(
                                selected = style == s,
                                onClick = { style = s },
                                label = { Text(s.label) },
                            )
                        }
                    }

                    val dialModifier = Modifier.padding(horizontal = 16.dp)
                    when (style) {
                        QiblaStyle.Modern -> QiblaDialModern(heading.degrees, qiblaDeg, aligned, dialModifier)
                        QiblaStyle.Classic -> QiblaDial(heading.degrees, qiblaDeg, aligned, dialModifier)
                        QiblaStyle.Vintage -> QiblaDialClassical(heading.degrees, qiblaDeg, aligned, dialModifier)
                    }

                    Text(
                        stringResource(Res.string.qibla_direction_degrees, qiblaDeg.roundToInt().toString()),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.onSurface,
                    )
                    Text(
                        stringResource(Res.string.distance_to_makkah_km, distance.roundToInt().toString()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceVariant,
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

                    if (!heading.accurate) {
                        Text(
                            stringResource(Res.string.compass_accuracy_low_wave_phone_in_figure_eight),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.warning,
                        )
                    }
                }
            }
        }
}

// ponytail: dev style switcher — labels hardcoded; move to strings if it ships permanently.
private enum class QiblaStyle(val label: String) {
    Modern("Modern"),
    Classic("Classic"),
    Vintage("Vintage"),
}
