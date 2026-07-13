package com.example.miqatapp.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.core.datetime.Now
import com.example.miqatapp.core.debug.Debug
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore

/** Fast-clock readout: confirms the current/next prayer stay in sync as the clock runs. Debug only. */
@Composable
fun SceneDebugOverlay(modifier: Modifier = Modifier) {
    if (!Debug.FAST_CLOCK) return
    val clock by Now.now.collectAsState()
    val now = clock.time
    val today by MiqatTimesStore.today.collectAsState()

    val prayerTimes = today.filter { it.miqat.isPrayer }
    val nextMt = prayerTimes.firstOrNull { it.at.time > now } ?: prayerTimes.firstOrNull()
    val sunriseTime = today.firstOrNull { it.miqat == Miqat.Sunrise }?.at?.time
    val startedPrayer = prayerTimes.lastOrNull { it.at.time <= now } ?: prayerTimes.lastOrNull()
    val currentPrayer = when {
        startedPrayer == null -> null
        startedPrayer.miqat == Miqat.Fajr && sunriseTime != null && now >= sunriseTime -> null
        else -> startedPrayer.miqat
    }

    Text(
        "${clock.date}  " + now.hour.toString().padStart(2, '0') + ":" + now.minute.toString().padStart(2, '0') +
            "   cur:" + (currentPrayer?.name ?: "-") + " next:" + (nextMt?.miqat?.name ?: "-"),
        color = Color.White,
        fontSize = 12.sp,
        modifier = modifier.background(Color.Black.copy(alpha = 0.55f)).padding(horizontal = 12.dp, vertical = 6.dp),
    )
}
