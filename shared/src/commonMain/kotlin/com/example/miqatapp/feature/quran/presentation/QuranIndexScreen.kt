package com.example.miqatapp.feature.quran.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppChip
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.components.TilePosition
import com.example.miqatapp.feature.quran.data.QuranRepository
import com.example.miqatapp.feature.quran.presentation.components.BookmarkRow
import com.example.miqatapp.feature.quran.presentation.components.HighlightRow
import com.example.miqatapp.feature.quran.presentation.components.JuzRow
import com.example.miqatapp.feature.quran.presentation.components.NoteRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuranIndexScreen(onOpen: (Int) -> Unit) {
    val juzs by produceState(emptyList()) { value = QuranRepository.juzs() }
    val tabs = QuranTab.entries
    val pager = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()
    val drawerState = LocalDrawerState.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Quran") },
                navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            ContinueCard { onOpen(1) }
            val chipState = rememberLazyListState()
            LaunchedEffect(pager.currentPage) { chipState.animateScrollToItem(pager.currentPage) } // keep the selected chip in view
            LazyRow(
                state = chipState,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(tabs.size) { i ->
                    AppChip(tabs[i].label, pager.currentPage == i, { scope.launch { pager.animateScrollToPage(i) } }, tabs[i].icon)
                }
            }
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                when (tabs[page]) {
                    QuranTab.Surah -> SurahTab(onOpen = onOpen)
                    else -> LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        when (tabs[page]) {
                            QuranTab.Juz -> items(juzs.size, key = { juzs[it].number }) { i ->
                                val juz = juzs[i]
                                JuzRow(juz, onOpen = { onOpen(juz.startsAt.id) }, onOpenSurah = { onOpen(it.startId) })
                            }
                            QuranTab.Bookmarks -> items(DUMMY_BOOKMARKS.size) { i -> val b = DUMMY_BOOKMARKS[i]; BookmarkRow(b.surah, b.ayah, b.text, TilePosition.at(i, DUMMY_BOOKMARKS.size)) { onOpen(b.id) } }
                            QuranTab.Notes -> items(DUMMY_NOTES.size) { i -> val n = DUMMY_NOTES[i]; NoteRow(n.surah, n.ayah, n.text, TilePosition.at(i, DUMMY_NOTES.size)) { onOpen(n.id) } }
                            QuranTab.Highlights -> items(DUMMY_HIGHLIGHTS.size) { i -> val h = DUMMY_HIGHLIGHTS[i]; HighlightRow(h.surah, h.ayah, h.text, h.color, TilePosition.at(i, DUMMY_HIGHLIGHTS.size)) { onOpen(h.id) } }
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

// basic hero — real last-read + progress come with that store
@Composable
private fun ContinueCard(onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceContainerHigh).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.BookOpen, null, tint = colors.primary)
        Spacer(Modifier.size(12.dp))
        Column {
            Text("Continue reading", color = colors.onSurface, fontWeight = FontWeight.SemiBold)
            Text("Start from the beginning", color = colors.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

// dummy data just to preview the Saved/Notes/Highlights layout — replace with real stores later
private class Saved(val id: Int, val surah: Int, val ayah: Int, val text: String, val color: Color = Color(0xFF7CB342))
private val DUMMY_BOOKMARKS = listOf(
    Saved(262, 2, 255, "اللَّهُ لَا إِلَـٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ"),
    Saved(3706, 36, 1, "يس"),
    Saved(6222, 112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ"),
)
private val DUMMY_NOTES = listOf(
    Saved(293, 2, 286, "A dua to memorize for daily use"),
    Saved(5, 1, 5, "The heart of Al-Fatiha"),
)
private val DUMMY_HIGHLIGHTS = listOf(
    Saved(4914, 55, 13, "فَبِأَيِّ آلَاءِ رَبِّكُمَا تُكَذِّبَانِ", Color(0xFF7CB342)),
    Saved(6, 1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", Color(0xFFFBC02D)),
)
