package com.example.miqatapp.feature.azkar.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.widgets.AppButton
import com.example.miqatapp.core.widgets.AppTextField
import com.example.miqatapp.core.widgets.AppTile
import com.example.miqatapp.core.widgets.TilePosition
import com.example.miqatapp.feature.duas.presentation.Dua
import com.example.miqatapp.feature.duas.presentation.DuaSection
import com.example.miqatapp.feature.duas.presentation.duasOf
import com.example.miqatapp.feature.tasbih.presentation.CountSheet
import com.example.miqatapp.feature.tasbih.presentation.CountTag
import com.example.miqatapp.feature.tasbih.presentation.SelectCheck

// ponytail: strings hardcoded until the design is locked; in-memory store until the DB lands.

/** Collections the user builds. Untyped (can mix any azkar) — they live in "All" + "Your collections". */
object UserCollections {
    data class UserColl(val id: String, val name: String, val items: List<Dua>)
    val list = mutableStateListOf<UserColl>()
    private var seq = 0
    fun add(name: String, items: List<Dua>) { list.add(0, UserColl("uc-${seq++}", name, items)) }
}

private enum class PKind { Dua, Zikr, Tasbih }
private enum class PTab(val label: String) { All("All"), Dua("Du'a"), Zikr("Zikr"), Tasbih("Tasbih") }

// behavior falls out of the section: everyday = read du'a, adhkar = zikr, glorifications = tasbih
private fun kindOf(section: DuaSection): PKind = when (section) {
    DuaSection.Everyday -> PKind.Dua
    DuaSection.Morning, DuaSection.Evening, DuaSection.AfterPrayer -> PKind.Zikr
    DuaSection.Tasbihat -> PKind.Tasbih
}

private fun tabKind(tab: PTab): PKind? = when (tab) {
    PTab.All -> null
    PTab.Dua -> PKind.Dua
    PTab.Zikr -> PKind.Zikr
    PTab.Tasbih -> PKind.Tasbih
}

/** Flat pool of every pickable azkar, tagged by behavior. */
private val POOL: List<Pair<Dua, PKind>> = DuaSection.entries.flatMap { s -> duasOf(s).map { it to kindOf(s) } }

/** Full-screen builder: name it (required), pick azkar across kinds, set each count via the shared CountSheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCollectionScreen(onBack: () -> Unit) {
    val c = AppTheme.colors
    var name by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(PTab.All) }
    val order = remember { mutableStateListOf<String>() }     // picked ids, in pick order
    val counts = remember { mutableStateMapOf<String, Int>() } // per-picked count
    var countSheetFor by remember { mutableStateOf<Dua?>(null) }
    val pool = POOL.filter { tabKind(tab)?.let { k -> it.second == k } ?: true }

    fun toggle(d: Dua) {
        if (d.id in order) { order.remove(d.id); counts.remove(d.id) }
        else { order.add(d.id); counts[d.id] = d.repeat }
    }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New collection", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                AppButton(
                    text = "Create collection (${order.size})",
                    onClick = {
                        val items = order.map { id -> POOL.first { it.first.id == id }.first.copy(repeat = counts[id] ?: 1) }
                        UserCollections.add(name.trim(), items)
                        onBack()
                    },
                    enabled = name.isNotBlank() && order.isNotEmpty(), // name is compulsory
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) {
                AppTextField(value = name, onValueChange = { name = it }, placeholder = "Collection name")
            }
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PTab.entries.forEach { t ->
                    val sel = t == tab
                    Box(
                        Modifier.clip(RoundedCornerShape(999.dp)).background(if (sel) c.primary else c.cardColor).clickable { tab = t }.padding(horizontal = 16.dp, vertical = 9.dp),
                    ) { Text(t.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (sel) c.onPrimary else c.onSurfaceVariant) }
                }
            }
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                itemsIndexed(pool, key = { _, p -> p.first.id }) { i, (d, _) ->
                    val pos = when {
                        pool.size == 1 -> TilePosition.Single
                        i == 0 -> TilePosition.First
                        i == pool.lastIndex -> TilePosition.Last
                        else -> TilePosition.Middle
                    }
                    AppTile(
                        title = d.arabic,            // Arabic first — same as the Tasbih tiles
                        subtitle = d.transliteration,
                        selected = d.id in order,
                        position = pos,
                        onClick = { toggle(d) },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CountTag(counts[d.id] ?: d.repeat) { countSheetFor = d }
                                Spacer(Modifier.width(4.dp))
                                SelectCheck(d.id in order) { toggle(d) }
                            }
                        },
                    )
                }
            }
        }
    }

    // your existing count picker — presets + ± quick-adjust
    countSheetFor?.let { d ->
        CountSheet(current = counts[d.id] ?: d.repeat, onPick = { counts[d.id] = it }, onDismiss = { countSheetFor = null })
    }
}
