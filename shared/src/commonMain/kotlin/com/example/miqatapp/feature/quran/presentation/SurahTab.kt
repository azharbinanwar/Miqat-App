package com.example.miqatapp.feature.quran.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.TilePosition
import com.example.miqatapp.feature.quran.data.QuranRepository
import com.example.miqatapp.feature.quran.data.QuranStore
import com.example.miqatapp.feature.quran.data.Surah
import com.example.miqatapp.feature.quran.presentation.components.SurahActionSheet
import com.example.miqatapp.feature.quran.presentation.components.SurahRow

// Surah listing — favorites pinned on top, then the rest; long-press a row for its actions
@Composable
fun SurahTab(onOpen: (Int) -> Unit) {
    val surahs by produceState(emptyList()) { value = QuranRepository.surahs() }
    val favorites by QuranStore.favorites.collectAsState()
    var actionSurah by remember { mutableStateOf<Surah?>(null) }

    val favs = surahs.filter { it.number in favorites }
    val rest = surahs.filterNot { it.number in favorites }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (favs.isNotEmpty()) {
            item { SectionLabel("Favorites") }
            items(favs.size) { i -> SurahRow(favs[i], TilePosition.at(i, favs.size), onLongClick = { actionSurah = favs[i] }) { onOpen(favs[i].startId) } }
            item { SectionLabel("Surahs") }
        }
        items(rest.size) { i -> SurahRow(rest[i], TilePosition.at(i, rest.size), onLongClick = { actionSurah = rest[i] }) { onOpen(rest[i].startId) } }
    }

    actionSurah?.let { s ->
        SurahActionSheet(
            surah = s,
            favorite = s.number in favorites,
            onToggleFavorite = { QuranStore.toggleFavorite(s.number) },
            onOpen = { onOpen(s.startId) },
            onDismiss = { actionSurah = null },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = AppTheme.colors.primary,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
    )
}
