package com.example.miqatapp.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.CircleDot
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme

/** A prayer's timing state relative to now. Icon/label here; colors in AppColors. */
enum class PrayerTimeStatus(val label: String, val icon: ImageVector) {
    Current("Now", Lucide.CircleDot),
    Soon("Soon", Lucide.Clock),
    Upcoming("Upcoming", Lucide.CalendarClock),
}

val PrayerTimeStatus.color: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            PrayerTimeStatus.Current -> it.currentColor
            PrayerTimeStatus.Soon -> it.soonColor
            PrayerTimeStatus.Upcoming -> it.upcomingColor
        }
    }

val PrayerTimeStatus.onColor: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            PrayerTimeStatus.Current -> it.onCurrentColor
            PrayerTimeStatus.Soon -> it.onSoonColor
            PrayerTimeStatus.Upcoming -> it.onUpcomingColor
        }
    }
