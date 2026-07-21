package com.example.miqatapp.feature.quran.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.quran.data.AyahRef
import com.example.miqatapp.feature.quran.data.QuranRepository
import com.example.miqatapp.feature.quran.data.QuranStore
import com.example.miqatapp.feature.quran.presentation.components.AyahActionSheet
import com.example.miqatapp.feature.quran.presentation.components.QuranCalligraphy
import com.example.miqatapp.feature.quran.presentation.components.RukuBlock
import com.example.miqatapp.feature.quran.toArabicIndic
import com.example.miqatapp.feature.quran.toSurahKey
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_surah_name
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.Font
import kotlin.time.Duration.Companion.milliseconds

// whole Quran as one continuous scroll, verses paged 100 at a time, grouped into rukus by the UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(startId: Int = 1, onBack: () -> Unit) {
    val ayahs = remember { mutableStateListOf<Ayah>() }
    val listState = rememberLazyListState()

    // load the whole Quran once; LazyColumn only renders what's on screen, so this stays cheap
    LaunchedEffect(Unit) { if (ayahs.isEmpty()) ayahs.addAll(QuranRepository.all()) }

    // ponytail: temporary — keep the calligraphy on screen at least 2s just to preview it; drop this later
    var minSplash by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(500.milliseconds); minSplash = true }

    if (ayahs.isEmpty() || !minSplash) {
        QuranCalligraphy()
        return
    }

    val colors = AppTheme.colors
    val fontSize by QuranStore.fontSize.collectAsState()
    val script by QuranStore.font.collectAsState()
    val surahFont = FontFamily(Font(Res.font.quran_surah_name)) // top-bar surah name
    val rukus = remember(ayahs.size) { groupByRuku(ayahs) }

    // jump to the ruku holding the opened ayah, once; then the user scrolls freely both ways
    LaunchedEffect(rukus) {
        val target = rukus.indexOfFirst { it.last().id >= startId }.coerceAtLeast(0)
        if (target > 0) listState.scrollToItem(target)
    }

    var selected by remember { mutableStateOf<AyahRef?>(null) }
    var expanded by remember(selected) { mutableStateOf(false) }
    val header by remember(rukus) { derivedStateOf { rukus.getOrNull(listState.firstVisibleItemIndex)?.firstOrNull() } }
    val blurRadius by animateDpAsState(if (expanded) 14.dp else 0.dp, label = "pageBlur")

    Box(Modifier.fillMaxSize().background(colors.background)) {
            Column(Modifier.fillMaxSize().blur(blurRadius)) {
                TopAppBar(
                    title = {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(header?.let { "الجزء ${it.juz.toArabicIndic()}" } ?: "", color = colors.onSurfaceVariant, fontSize = 14.sp)
                            Spacer(Modifier.weight(1f))
                            header?.let { Text(it.surah.toSurahKey(), fontFamily = surahFont, fontSize = 22.sp, color = colors.primary) }
                        }
                    },
                    navigationIcon = { IconButton(onBack) { Icon(Lucide.ChevronLeft, "Back", tint = colors.onSurface) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
                HorizontalDivider(color = colors.outlineVariant)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val tapAway = if (selected != null && !expanded) Modifier.pointerInput(Unit) { detectTapGestures { selected = null } } else Modifier
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().then(tapAway)) {
                        items(rukus.size) { i ->
                            val ruku = rukus[i]
                            // no divider when the next ruku starts a new surah (the header already separates)
                            val divider = rukus.getOrNull(i + 1)?.firstOrNull()?.surah == ruku.first().surah
                            RukuBlock(ruku, divider, selected) { selected = if (selected == it) null else it }
                        }
                    }
                }
            }

            selected?.let { ref ->
                AyahActionSheet(
                    label = "Surah ${ref.surah} · Ayah ${ref.ayah}",
                    fontSize = fontSize,
                    onFontChange = { QuranStore.setFontSize(it) },
                    font = script,
                    onFontSelect = { QuranStore.setFont(it) },
                    onExpandedChange = { expanded = it },
                    onDismiss = { selected = null },
                )
            }
        }
    }

// split verses into rukus (a run ends where endsRuku is true)
private fun groupByRuku(ayahs: List<Ayah>): List<List<Ayah>> {
    val out = ArrayList<List<Ayah>>()
    var run = ArrayList<Ayah>()
    for (a in ayahs) { run.add(a); if (a.endsRuku) { out.add(run); run = ArrayList() } }
    if (run.isNotEmpty()) out.add(run)
    return out
}
