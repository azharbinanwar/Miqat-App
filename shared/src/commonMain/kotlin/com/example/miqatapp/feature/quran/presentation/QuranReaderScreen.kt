package com.example.miqatapp.feature.quran.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Palette
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.quran.data.Ayah
import com.example.miqatapp.feature.quran.data.AyahRef
import com.example.miqatapp.feature.quran.data.QuranRepository
import com.example.miqatapp.feature.quran.data.QuranStore
import com.example.miqatapp.feature.quran.presentation.components.AyahActionSheet
import com.example.miqatapp.feature.quran.presentation.components.QuranCalligraphy
import com.example.miqatapp.feature.quran.presentation.components.ReaderSettingsSheet
import com.example.miqatapp.feature.quran.presentation.components.RukuBlock
import com.example.miqatapp.feature.quran.toArabicIndic
import com.example.miqatapp.feature.quran.toJuzKey
import com.example.miqatapp.feature.quran.toSurahKey
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_juz
import com.example.miqatapp.resources.quran_surah_name
import org.jetbrains.compose.resources.Font

// whole Quran as one continuous scroll, verses paged 100 at a time, grouped into rukus by the UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(startId: Int = 1, onBack: () -> Unit) {
    val ayahs = remember { mutableStateListOf<Ayah>() }
    val listState = rememberLazyListState()

    // load the whole Quran once; LazyColumn only renders what's on screen, so this stays cheap
    LaunchedEffect(Unit) { if (ayahs.isEmpty()) ayahs.addAll(QuranRepository.all()) }

    // stays true only after the list has loaded AND jumped to the target — the splash overlay covers until then
    var scrolled by remember { mutableStateOf(false) }

    val colors = AppTheme.colors
    val fontSize by QuranStore.fontSize.collectAsState()
    val script by QuranStore.font.collectAsState()
    val surahFont = FontFamily(Font(Res.font.quran_surah_name)) // top-bar surah name
    val juzFont = FontFamily(Font(Res.font.quran_juz))
    val rukus = remember(ayahs.size) { groupByRuku(ayahs) }
    // for each ruku: its number within its surah, and whether it's the surah's last ruku (then no "next")
    val rukuMeta = remember(rukus) {
        val meta = ArrayList<Pair<Int, Boolean>>(rukus.size)
        var surah = -1
        var n = 0
        rukus.forEach { r ->
            val s = r.first().surah
            n = if (s != surah) {
                surah = s; 1
            } else n + 1
            meta.add(n to false)
        }
        for (i in rukus.indices) {
            val lastInSurah = i == rukus.lastIndex || rukus[i + 1].first().surah != rukus[i].first().surah
            meta[i] = meta[i].first to lastInSurah
        }
        meta
    }

    // jump to the ruku holding the opened ayah, once; then the user scrolls freely both ways
    LaunchedEffect(rukus) {
        if (rukus.isNotEmpty()) {
            val target = rukus.indexOfFirst { it.last().id >= startId }.coerceAtLeast(0)
            if (target > 0) listState.scrollToItem(target)
            scrolled = true
        }
    }

    var selected by remember { mutableStateOf<AyahRef?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var expanded by remember(selected) { mutableStateOf(false) }
    val header by remember(rukus) { derivedStateOf { rukus.getOrNull(listState.firstVisibleItemIndex)?.firstOrNull() } }
    val blurRadius by animateDpAsState(if (expanded) 14.dp else 0.dp, label = "pageBlur")

    Box(Modifier.fillMaxSize().background(colors.background)) {
            Column(Modifier.fillMaxSize().blur(blurRadius)) {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(header?.let { "${it.juz.toArabicIndic()} - " } ?: "", color = colors.primary, fontSize = 14.sp)
                                Text(header?.juz?.toJuzKey() ?: "", fontFamily = juzFont, color = colors.primary, fontSize = 14.sp)
                            }
                            Text("  .  ", color = colors.primary, fontSize = 14.sp)
                            header?.let { Text(it.surah.toSurahKey(), fontFamily = surahFont, fontSize = 28.sp, color = colors.primary) }
                        }
                    },
                    navigationIcon = { IconButton(onBack) { Icon(Lucide.ChevronLeft, "Back", tint = colors.onSurface) } },
                    actions = { IconButton({ showSettings = true }) { Icon(Lucide.Palette, "Reading settings", tint = colors.onSurface) } },
                )
                HorizontalDivider(color = colors.outlineVariant)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val tapAway = if (selected != null && !expanded) Modifier.pointerInput(Unit) { detectTapGestures { selected = null } } else Modifier
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().then(tapAway)) {
                        items(rukus.size) { i ->
                            val ruku = rukus[i]
                            // juz of the ayah before this ruku, so RukuBlock can mark a juz that begins inside it
                            val prevJuz = if (i == 0) ruku.first().juz else rukus[i - 1].last().juz
                            val (numInSurah, lastInSurah) = rukuMeta[i]
                            RukuBlock(ruku, numInSurah, if (lastInSurah) null else numInSurah + 1, prevJuz, selected) {
                                selected = if (selected == it) null else it
                            }
                        }
                    }
                }
            }

            selected?.let { ref ->
                AyahActionSheet(
                    label = "Surah ${ref.surah} · Ayah ${ref.ayah}",
                    onExpandedChange = { expanded = it },
                    onDismiss = { selected = null },
                )
            }

            if (showSettings) {
                ReaderSettingsSheet(
                    fontSize = fontSize,
                    onFontChange = { QuranStore.setFontSize(it) },
                    font = script,
                    onFontSelect = { QuranStore.setFont(it) },
                    onDismiss = { showSettings = false },
                )
            }

        // calligraphy overlay on top while loading + jumping, so the scroll jump is hidden; fades out when ready
        AnimatedVisibility(visible = !scrolled, enter = EnterTransition.None, exit = fadeOut()) {
            QuranCalligraphy()
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
