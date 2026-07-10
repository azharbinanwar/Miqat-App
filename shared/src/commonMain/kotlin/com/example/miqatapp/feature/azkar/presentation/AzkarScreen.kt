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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.List
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.icons.TasbihIcon
import com.example.miqatapp.core.widgets.AppAction
import com.example.miqatapp.core.widgets.AppTileGroup
import com.example.miqatapp.core.widgets.AppTileItem
import com.example.miqatapp.core.navigation.AppRoute
import com.example.miqatapp.core.navigation.LocalNavController
import com.example.miqatapp.core.widgets.LocalDrawerState
import com.example.miqatapp.feature.duas.presentation.AzkarCollectionReader
import com.example.miqatapp.feature.duas.presentation.AzkarListReader
import com.example.miqatapp.feature.duas.presentation.Dua
import com.example.miqatapp.feature.duas.presentation.DuaSection
import com.example.miqatapp.feature.duas.presentation.DuaStore
import com.example.miqatapp.feature.duas.presentation.duasOf
import com.example.miqatapp.feature.duas.presentation.queueForBeads
import com.example.miqatapp.feature.tasbih.presentation.CountTag
import com.example.miqatapp.feature.tasbih.presentation.HeartIcon
import kotlinx.coroutines.launch

// ponytail: strings hardcoded until the design is locked, then localize in one pass.

private enum class ZTab(val label: String) { All("All"), Dua("Du'a"), Zikr("Zikr"), Tasbih("Tasbih") }

// everyday = du'a, glorifications = tasbih, the rest of the adhkar = zikr (Tasbih is a tab here, not a drawer screen)
private fun kindOf(s: DuaSection): ZTab = when (s) {
    DuaSection.Everyday -> ZTab.Dua
    DuaSection.Tasbihat -> ZTab.Tasbih
    else -> ZTab.Zikr
}
private fun nameOf(s: DuaSection): String = when (s) {
    DuaSection.Morning -> "Morning"
    DuaSection.Evening -> "Evening"
    DuaSection.AfterPrayer -> "After prayer"
    DuaSection.Everyday -> "Everyday du'a"
    DuaSection.Tasbihat -> "Tasbihat"
}
private fun iconOf(s: DuaSection): ImageVector = when (s) {
    DuaSection.Morning -> Lucide.Sunrise
    DuaSection.Evening -> Lucide.Sunset
    DuaSection.AfterPrayer -> Lucide.Sparkles
    DuaSection.Everyday -> Lucide.BookOpen
    DuaSection.Tasbihat -> TasbihIcon
}

private const val PEEK = 5

/** One home for du'a / zikr: Favorites + Your collections on top, then each category peeks 5 with a heart; See all → reader. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarScreen() {
    val c = AppTheme.colors
    val drawerState = LocalDrawerState.current
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf(ZTab.All) }

    var openSection by remember { mutableStateOf<DuaSection?>(null) }
    var openColl by remember { mutableStateOf<UserCollections.UserColl?>(null) }
    var creating by remember { mutableStateOf(false) }
    var showFavs by remember { mutableStateOf(false) }

    val favDuas = DuaSection.entries.flatMap { duasOf(it) }.filter { it.id in DuaStore.favIds }

    openSection?.let { s -> AzkarCollectionReader(s, onBack = { openSection = null }); return }
    openColl?.let { coll -> AzkarListReader(coll.name, coll.items, onBack = { openColl = null }); return }
    if (showFavs) { AzkarListReader("Favorites", favDuas, onBack = { showFavs = false }); return }
    if (creating) { CreateCollectionScreen(onBack = { creating = false }); return }

    val categories = DuaSection.entries.filter { tab == ZTab.All || kindOf(it) == tab }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Azkar & Du'a", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { ContinueCard() }
            item { TodayCard() }
            item { TabRow(tab) { tab = it } }

            // Favorites + Your collections are untyped → only on All
            if (tab == ZTab.All && favDuas.isNotEmpty()) {
                item {
                    AppTileGroup(
                        items = listOf(
                            AppTileItem(title = "Favorites", actions = listOf(
                                AppAction(TasbihIcon) { queueForBeads(favDuas); nav.navigate(AppRoute.TasbihCounter) },
                                AppAction(Lucide.ChevronRight) { showFavs = true },
                            )),
                        ) + favDuas.take(PEEK).map { d -> azkarItem(d, onOpen = { showFavs = true }, onBeads = { queueForBeads(listOf(d)); nav.navigate(AppRoute.TasbihCounter) }) },
                    )
                }
            }
            if (tab == ZTab.All && UserCollections.list.isNotEmpty()) {
                item {
                    AppTileGroup(
                        items = listOf(AppTileItem(title = "Your collections")) + UserCollections.list.map { uc ->
                            AppTileItem(title = uc.name, subtitle = "${uc.items.size} items", leadingIcon = Lucide.Star, onClick = { openColl = uc })
                        },
                    )
                }
            }

            // each category: header + up to PEEK azkar (heart each), then "See all (N)"
            categories.forEach { section ->
                val all = duasOf(section)
                item {
                    AppTileGroup(
                        items = listOf(
                            AppTileItem(title = nameOf(section), actions = listOf(
                                AppAction(TasbihIcon) { queueForBeads(all); nav.navigate(AppRoute.TasbihCounter) },
                                AppAction(Lucide.ChevronRight) { openSection = section },
                            )),
                        ) + all.take(PEEK).map { d -> azkarItem(d, onOpen = { openSection = section }, onBeads = { queueForBeads(listOf(d)); nav.navigate(AppRoute.TasbihCounter) }) },
                    )
                }
            }

            item {
                AppTileGroup(items = listOf(AppTileItem(title = "New collection", leadingIcon = Lucide.Plus, onClick = { creating = true })))
            }
        }
    }
}

/** A peek row: Arabic title, transliteration subtitle, its default reps (tap → beads), and the filled/outline heart. */
private fun azkarItem(d: Dua, onOpen: () -> Unit, onBeads: () -> Unit) = AppTileItem(
    title = d.arabic,
    subtitle = d.transliteration,
    onClick = onOpen,
    trailing = {
        val c = AppTheme.colors
        val fav = d.id in DuaStore.favIds
        Row(verticalAlignment = Alignment.CenterVertically) {
            CountTag(d.repeat) { onBeads() }   // tap the reps → open the Tasbih beads counter
            Spacer(Modifier.width(4.dp))
            HeartIcon(
                filled = fav,
                tint = if (fav) c.primary else c.onSurfaceVariant,
                size = 20.dp,
                modifier = Modifier.clickable { if (fav) DuaStore.favIds.remove(d.id) else DuaStore.favIds.add(d.id) },
            )
        }
    },
)

private fun seeAllItem(n: Int, onOpen: () -> Unit) =
    AppTileItem(title = "See all ($n)", leadingIcon = Lucide.List, onClick = onOpen)

/** Resume where you left off — reads the live session later; static for the preview. */
@Composable
private fun ContinueCard() {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.primary).clickable { /* ponytail: resume session */ }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("CONTINUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = c.onPrimary.copy(alpha = 0.8f))
            Spacer(Modifier.height(4.dp))
            Text("After prayer  ·  3 / 7", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = c.onPrimary)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.onPrimary.copy(alpha = 0.25f))) {
                Box(Modifier.fillMaxWidth(3f / 7f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.onPrimary))
            }
        }
        Spacer(Modifier.width(14.dp))
        Box(Modifier.size(40.dp).clip(CircleShape).background(c.onPrimary.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Icon(Lucide.Play, null, tint = c.onPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

/** The day's pick — a daily rotation later; static for the preview. */
@Composable
private fun TodayCard() {
    val c = AppTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(c.cardColor).padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            Icon(Lucide.Sparkles, null, tint = c.primary, modifier = Modifier.size(15.dp))
            Text("TODAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp, color = c.primary)
        }
        Spacer(Modifier.height(14.dp))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
                Modifier.fillMaxWidth(),
                fontSize = 24.sp, fontWeight = FontWeight.Medium, lineHeight = 44.sp,
                textAlign = TextAlign.Start, color = c.onSurface,
            )
        }
        Spacer(Modifier.height(12.dp))
        Text("Glory is to Allah and praise is to Him.", fontSize = 14.sp, color = c.onSurfaceVariant, lineHeight = 21.sp)
    }
}

@Composable
private fun TabRow(selected: ZTab, onSelect: (ZTab) -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ZTab.entries.forEach { t ->
            val sel = t == selected
            Box(
                Modifier.clip(RoundedCornerShape(999.dp)).background(if (sel) c.primary else c.cardColor).clickable { onSelect(t) }.padding(horizontal = 16.dp, vertical = 9.dp),
            ) { Text(t.label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (sel) c.onPrimary else c.onSurfaceVariant) }
        }
    }
}
