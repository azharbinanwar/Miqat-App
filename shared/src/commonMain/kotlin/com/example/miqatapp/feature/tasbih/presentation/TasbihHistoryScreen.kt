package com.example.miqatapp.feature.tasbih.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.widgets.AppCard
import com.example.miqatapp.core.widgets.AppTile
import com.example.miqatapp.core.widgets.StateView
import com.example.miqatapp.core.widgets.TilePosition
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.tasbih_all_dhikr
import com.example.miqatapp.resources.tasbih_all_sessions
import com.example.miqatapp.resources.tasbih_azkar
import com.example.miqatapp.resources.tasbih_back
import com.example.miqatapp.resources.tasbih_dhikr
import com.example.miqatapp.resources.tasbih_history
import com.example.miqatapp.resources.tasbih_no_history
import com.example.miqatapp.resources.tasbih_no_history_hint
import com.example.miqatapp.resources.tasbih_sessions
import com.example.miqatapp.resources.tasbih_total_recited
import org.jetbrains.compose.resources.stringResource

/** One past completion. ponytail: mock for now — replaced by persisted history later. */
private data class HistoryEntry(val dhikrId: String, val dhikr: String, val arabic: String, val dateLabel: String, val count: Int)

/**
 * Dhikr history — generic: pass [dhikrId] to see one dhikr's history (the counter's "History"), or omit
 * it for all dhikr. A full screen so the [LazyColumn] can window many rows; each row is a plain [AppTile]
 * with an explicit [TilePosition] to keep the grouped rounded corners while staying lazy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihHistoryScreen(dhikrId: String? = null, onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    val sessions = remember(dhikrId) {
        val all = mockHistory()
        if (dhikrId == null) all else all.filter { it.dhikrId == dhikrId }
    }
    val single = dhikrId != null
    val totalRecited = sessions.sumOf { it.count }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tasbih_history), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Lucide.ArrowLeft, stringResource(Res.string.tasbih_back)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
            )
        },
    ) { pad ->
        if (sessions.isEmpty()) {
            StateView(
                title = stringResource(Res.string.tasbih_no_history),
                message = stringResource(Res.string.tasbih_no_history_hint),
                icon = { Icon(Lucide.History, null, tint = c.onSurfaceVariant.copy(alpha = 0.45f), modifier = Modifier.size(56.dp)) },
                modifier = Modifier.fillMaxSize().padding(pad),
            )
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                val first = sessions.firstOrNull()
                if (single) {
                    StatsHeader(
                        heading = first?.arabic ?: stringResource(Res.string.tasbih_dhikr),
                        subheading = first?.dhikr,
                        stats = listOf(totalRecited.toString() to stringResource(Res.string.tasbih_total_recited), sessions.size.toString() to stringResource(Res.string.tasbih_sessions)),
                    )
                } else {
                    StatsHeader(
                        heading = stringResource(Res.string.tasbih_all_dhikr),
                        subheading = null,
                        stats = listOf(
                            totalRecited.toString() to stringResource(Res.string.tasbih_total_recited),
                            sessions.size.toString() to stringResource(Res.string.tasbih_sessions),
                            sessions.map { it.dhikrId }.distinct().size.toString() to stringResource(Res.string.tasbih_azkar),
                        ),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(stringResource(Res.string.tasbih_all_sessions), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = c.primary, modifier = Modifier.padding(start = 4.dp, bottom = 4.dp))
            }
            itemsIndexed(sessions, key = { i, _ -> i }) { i, s ->
                AppTile(
                    // single dhikr → rows read by date; all → rows read by which dhikr (date as subtitle)
                    title = if (single) s.dateLabel else s.dhikr,
                    subtitle = if (single) null else s.dateLabel,
                    trailing = { Text(if (s.count == 0) "∞" else "×${s.count}", fontWeight = FontWeight.Bold, color = c.primary) },
                    position = when {
                        sessions.size == 1 -> TilePosition.Single
                        i == 0 -> TilePosition.First
                        i == sessions.lastIndex -> TilePosition.Last
                        else -> TilePosition.Middle
                    },
                )
            }
        }
    }
}

/** Heading (+ optional sub) and a row of stat blocks. */
@Composable
private fun StatsHeader(heading: String, subheading: String?, stats: List<Pair<String, String>>) {
    val c = AppTheme.colors
    AppCard(Modifier.fillMaxWidth()) {
        Text(heading, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
        if (subheading != null) Text(subheading, fontSize = 12.sp, color = c.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            stats.forEach { (value, label) -> Stat(value, label) }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    val c = AppTheme.colors
    Column {
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.primary)
        Text(label, fontSize = 12.sp, color = c.onSurfaceVariant)
    }
}

// ponytail: mock data so we can design the screen now; real entries come from persisted history.
private fun mockHistory(): List<HistoryEntry> {
    val dhikrs = listOf(
        Triple("subhanallah", "SubhanAllah", "سُبْحَانَ ٱللَّٰه"),
        Triple("alhamdulillah", "Alhamdulillah", "ٱلْحَمْدُ للَّٰه"),
        Triple("allahuakbar", "Allahu Akbar", "اللّٰه أكبر"),
        Triple("astaghfirullah", "Astaghfirullah", "أَسْتَغْفِرُ ٱللَّٰه"),
    )
    val labels = listOf("Today", "Yesterday", "12 Jun", "11 Jun", "10 Jun", "9 Jun", "8 Jun", "7 Jun")
    val counts = listOf(33, 99, 100, 33, 500, 33)
    return List(50) { i ->
        val (id, name, arabic) = dhikrs[i % dhikrs.size]
        HistoryEntry(id, name, arabic, labels[i % labels.size], counts[i % counts.size])
    }
}
