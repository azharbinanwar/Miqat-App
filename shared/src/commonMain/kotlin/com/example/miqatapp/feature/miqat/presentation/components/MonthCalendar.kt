package com.example.miqatapp.feature.miqat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.week_days
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringArrayResource

/** Month grid. Tap a day to select it; arrows change month. Monday-first (matches week_days). */
@Composable
fun MonthCalendar(
    year: Int,
    month: Int,
    selected: LocalDate,
    today: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
    dayDots: (LocalDate) -> List<Color> = { emptyList() }, // optional per-prayer dots under each day
) {
    val c = AppTheme.colors
    val first = LocalDate(year, month, 1)
    val daysInMonth = first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth
    val leadingBlanks = first.dayOfWeek.isoDayNumber - 1 // Monday = 0

    val cells = buildList<LocalDate?> {
        repeat(leadingBlanks) { add(null) }
        for (d in 1..daysInMonth) add(LocalDate(year, month, d))
    }

    Column(modifier.fillMaxWidth()) {
        // month header
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevMonth) { Icon(Lucide.ChevronLeft, null, tint = c.onSurface) }
            Text(
                "${monthName(month)} $year",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.onSurface,
            )
            IconButton(onClick = onNextMonth) { Icon(Lucide.ChevronRight, null, tint = c.onSurface) }
        }

        // weekday labels
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            stringArrayResource(Res.array.week_days).forEach { label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = c.onSurfaceVariant,
                )
            }
        }

        // day grid
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(Modifier.weight(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (date != null) DayCell(date, date == selected, date == today, dayDots(date), onSelect)
                    }
                }
                repeat(7 - week.size) { Box(Modifier.weight(1f)) } // pad short last week
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, selected: Boolean, today: Boolean, dots: List<Color>, onSelect: (LocalDate) -> Unit) {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().clickable { onSelect(date) },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var circle = Modifier.size(32.dp).clip(CircleShape)
        if (selected) circle = circle.background(c.primary)
        if (today && !selected) circle = circle.border(1.5.dp, c.primary, CircleShape)
        Box(circle, contentAlignment = Alignment.Center) {
            Text(
                date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected || today) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    selected -> c.onPrimary
                    today -> c.primary
                    else -> c.onSurface
                },
            )
        }
        if (dots.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Row(Modifier.height(5.dp), horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                dots.forEach { Box(Modifier.size(4.dp).clip(CircleShape).background(it)) }
            }
        }
    }
}

private fun monthName(month: Int): String =
    Month(month).name.lowercase().replaceFirstChar { it.uppercase() }
