package com.example.miqatapp.feature.qibla.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.enums.QiblaStyle

/** The single place that maps a [QiblaStyle] to its dial composable — screen and picker both use it. */
@Composable
fun QiblaDialFor(
    style: QiblaStyle,
    headingDeg: Float,
    qiblaDeg: Float,
    aligned: Boolean,
    modifier: Modifier = Modifier,
) {
    when (style) {
        QiblaStyle.Modern -> QiblaDialModern(headingDeg, qiblaDeg, aligned, modifier)
        QiblaStyle.Classic -> QiblaDial(headingDeg, qiblaDeg, aligned, modifier)
        QiblaStyle.CompassRose -> QiblaDialClassical(headingDeg, qiblaDeg, aligned, modifier)
    }
}

/**
 * Bottom-sheet picker with a live preview of each dial. Tapping a style applies it immediately
 * (the caller persists via the store), so the sheet can stay open to compare.
 */
@Composable
fun QiblaStyleSheet(
    current: QiblaStyle,
    headingDeg: Float,
    qiblaDeg: Float,
    aligned: Boolean,
    onSelect: (QiblaStyle) -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(onDismiss = onDismiss, title = "Compass style") {
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QiblaStyle.entries.forEach { style ->
                val selected = style == current
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) AppTheme.colors.primary else AppTheme.colors.outline.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(16.dp),
                        )
                        .clickable { onSelect(style) }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // draw the dial at its full design size, then scale the whole unit down so
                    // every element (circle, text, ticks, Kaaba, strokes) shrinks by the same factor
                    BoxWithConstraints(
                        Modifier.fillMaxWidth().aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        val reference = 240.dp
                        QiblaDialFor(
                            style, headingDeg, qiblaDeg, aligned,
                            Modifier.requiredSize(reference).scale(maxWidth / reference),
                        )
                    }
                    Text(
                        style.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}
