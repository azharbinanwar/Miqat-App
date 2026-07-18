package com.example.miqatapp.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.AppCard
import com.example.miqatapp.core.enums.WidgetColor
import com.example.miqatapp.feature.widget.WidgetConfig
import com.example.miqatapp.feature.widget.WidgetRefresher
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.add_to_home_screen
import com.example.miqatapp.resources.background_opacity
import com.example.miqatapp.resources.card_color
import com.example.miqatapp.resources.widgets
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/**
 * Prayer Card look editor. Same screen for both entry points:
 *  - Settings → Widgets: [onPrimary] pins a new card to the home screen.
 *  - Add-from-gallery configure: [onPrimary] confirms the placement (returns RESULT_OK).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetSettingsScreen(onBack: () -> Unit, onPrimary: () -> Unit) {
    val c = AppTheme.colors
    var color by remember { mutableStateOf(WidgetConfig.color()) }
    var opacity by remember { mutableFloatStateOf(WidgetConfig.opacity()) }

    // Persist + repaint every placed widget instantly on any change (look is one shared setting).
    fun apply() { WidgetConfig.save(opacity, color); WidgetRefresher.redraw() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.widgets)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Lucide.ChevronLeft, null) } },
            )
        },
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp)) {
            AppCard {
                Text(stringResource(Res.string.card_color), fontWeight = FontWeight.SemiBold, color = c.onSurface)
                Row(
                    Modifier.fillMaxWidth().padding(top = 12.dp).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    WidgetColor.entries.forEach { wc ->
                        val selected = wc == color
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(width = 64.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(wc.fill, wc.fillEnd)))
                                    .border(2.dp, if (selected) c.primary else Color.Transparent, RoundedCornerShape(12.dp))
                                    .clickable { color = wc; apply() },
                            ) {
                                // dot = the text/on colour, so fill + text read at a glance
                                Box(Modifier.align(Alignment.BottomEnd).padding(7.dp).size(11.dp).clip(CircleShape).background(wc.on))
                            }
                            Text(wc.label, fontSize = 11.sp, color = c.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(Res.string.background_opacity), fontWeight = FontWeight.SemiBold, color = c.onSurface)
                    Text("${(opacity * 100).roundToInt()}%", fontWeight = FontWeight.Bold, color = c.primary)
                }
                Slider(value = opacity, onValueChange = { opacity = it }, onValueChangeFinished = { apply() })
            }

            Spacer(Modifier.height(20.dp))
            AppButton(text = stringResource(Res.string.add_to_home_screen), onClick = onPrimary, modifier = Modifier.fillMaxWidth())
        }
    }
}
