package com.example.miqatapp.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.enums.WidgetColor
import com.example.miqatapp.feature.widget.PrayerBarPreview
import com.example.miqatapp.feature.widget.PrayerCardPreview
import com.example.miqatapp.feature.widget.PrayerCurrentPreview
import com.example.miqatapp.feature.widget.PrayerIconPreview
import com.example.miqatapp.feature.widget.PrayerMinimalPreview
import com.example.miqatapp.feature.widget.PrayerTilePreview
import com.example.miqatapp.feature.widget.PrayerTimesPreview
import com.example.miqatapp.feature.widget.WidgetKind
import com.example.miqatapp.feature.widget.WidgetStyle
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.background_opacity
import com.example.miqatapp.resources.card_color
import com.example.miqatapp.resources.widget_add
import com.example.miqatapp.resources.widget_bar
import com.example.miqatapp.resources.widget_card
import com.example.miqatapp.resources.widget_current
import com.example.miqatapp.resources.widget_icon
import com.example.miqatapp.resources.widget_minimal
import com.example.miqatapp.resources.widget_tile
import com.example.miqatapp.resources.widget_times
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

// One customize bottom sheet, used both in the in-app gallery and by the drop-time configure activity.
// Starts from [initial]; [onConfirm] gets the chosen style when the user taps the confirm button.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetCustomizeSheet(kind: WidgetKind, initial: WidgetStyle, confirmText: String? = null, onConfirm: (WidgetStyle) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    var color by remember { mutableStateOf(initial.color) }
    var opacity by remember { mutableFloatStateOf(initial.alpha) }

    AppBottomSheet(
        onDismiss = onDismiss,
        title = kind.displayName(),
        footer = {
            AppButton(
                confirmText ?: stringResource(Res.string.widget_add),
                onClick = { onConfirm(WidgetStyle(color.key, (opacity * 100).roundToInt())) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Box(Modifier.fillMaxWidth().padding(bottom = 20.dp), contentAlignment = Alignment.Center) {
            WidgetPreview(kind, color, opacity)
        }
        Text(stringResource(Res.string.card_color), fontWeight = FontWeight.SemiBold, color = c.onSurface)
        Row(Modifier.fillMaxWidth().padding(top = 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WidgetColor.entries.forEach { wc ->
                Box(
                    Modifier.size(width = 56.dp, height = 42.dp).clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(wc.fill, wc.fillEnd)))
                        .border(2.dp, if (wc == color) c.primary else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { color = wc },
                ) { Box(Modifier.align(Alignment.BottomEnd).padding(6.dp).size(10.dp).clip(CircleShape).background(wc.on)) }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(Res.string.background_opacity), fontWeight = FontWeight.SemiBold, color = c.onSurface)
            Text("${(opacity * 100).roundToInt()}%", fontWeight = FontWeight.Bold, color = c.primary)
        }
        Slider(value = opacity, onValueChange = { opacity = it })
    }
}

// The live preview for a widget kind at the given colour + opacity.
@Composable
internal fun WidgetPreview(kind: WidgetKind, color: WidgetColor, opacity: Float) {
    when (kind) {
        WidgetKind.Times -> PrayerTimesPreview(color, opacity)
        WidgetKind.Bar -> PrayerBarPreview(color, opacity)
        WidgetKind.Card -> PrayerCardPreview(color, opacity)
        WidgetKind.Minimal -> PrayerMinimalPreview(color, opacity)
        WidgetKind.Current -> PrayerCurrentPreview(color, opacity)
        WidgetKind.Tile -> PrayerTilePreview(color, opacity)
        WidgetKind.Icon -> PrayerIconPreview(color, opacity)
    }
}

// Localized display name for the gallery + sheet (mirrors the android widget-picker labels).
@Composable
internal fun WidgetKind.displayName(): String = stringResource(
    when (this) {
        WidgetKind.Times -> Res.string.widget_times
        WidgetKind.Bar -> Res.string.widget_bar
        WidgetKind.Card -> Res.string.widget_card
        WidgetKind.Minimal -> Res.string.widget_minimal
        WidgetKind.Current -> Res.string.widget_current
        WidgetKind.Tile -> Res.string.widget_tile
        WidgetKind.Icon -> Res.string.widget_icon
    },
)
