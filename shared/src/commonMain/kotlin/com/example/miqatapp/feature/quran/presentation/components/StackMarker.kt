package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.quran.data.QuranStore
import com.example.miqatapp.feature.quran.data.QuranSymbols
import com.example.miqatapp.feature.quran.toArabicIndic
import com.example.miqatapp.feature.quran.toJuzKey
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_juz
import com.example.miqatapp.resources.tanzil_hafs
import org.jetbrains.compose.resources.Font

// ruku end: big ع (lifted to sit centered on the lines) with this ruku's number on the right, next on the left
@Composable
fun RukuMarker(rukuNumber: Int, nextRukuNumber: Int?, modifier: Modifier = Modifier) {
    val ainFont = FontFamily(Font(Res.font.tanzil_hafs))
    MarkerRow(QuranSymbols.RUKU, ainFont, lift = 0.5f, leftLabel = nextRukuNumber?.toArabicIndic(), rightLabel = rukuNumber.toArabicIndic(), modifier = modifier)
}

// juz start: ornate juz name between the lines (no lift — different font metrics — and no numbers)
@Composable
fun JuzMarker(juz: Int, modifier: Modifier = Modifier) {
    val juzFont = FontFamily(Font(Res.font.quran_juz))
    MarkerRow(juz.toJuzKey(), juzFont, lift = 0f, leftLabel = null, rightLabel = null, modifier = modifier)
}

// big centered glyph with a 3-line ornament each side + optional numbers; `lift` nudges the glyph up (× font size)
@Composable
private fun MarkerRow(middle: String, middleFont: FontFamily, lift: Float, leftLabel: String?, rightLabel: String?, modifier: Modifier) {
    val fontSize by QuranStore.fontSize.collectAsState()
    val color = AppTheme.colors.primary
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SideLines(color, Alignment.End, Modifier.weight(1f))
            if (leftLabel != null) Text(leftLabel, color = color, fontSize = (fontSize * 0.8f).sp, modifier = Modifier.padding(start = 10.dp, end = 4.dp))
            Text(
                middle, fontFamily = middleFont, color = color, fontSize = (fontSize * 1.7f).sp, maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp).offset(y = -(fontSize * lift).dp),
            )
            if (rightLabel != null) Text(rightLabel, color = color, fontSize = (fontSize * 0.8f).sp, modifier = Modifier.padding(start = 4.dp, end = 10.dp))
            SideLines(color, Alignment.Start, Modifier.weight(1f))
        }
    }
}

// three stacked lines toward the glyph — top & bottom short, middle long
@Composable
private fun SideLines(color: Color, align: Alignment.Horizontal, modifier: Modifier) {
    Column(modifier, horizontalAlignment = align, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Box(Modifier.fillMaxWidth(0.6f).height(1.5.dp).background(color))
        Box(Modifier.fillMaxWidth().height(1.5.dp).background(color))
        Box(Modifier.fillMaxWidth(0.6f).height(1.5.dp).background(color))
    }
}
