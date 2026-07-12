package com.example.miqatapp.core.datetime

import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.friday
import com.example.miqatapp.resources.monday
import com.example.miqatapp.resources.saturday
import com.example.miqatapp.resources.sunday
import com.example.miqatapp.resources.thursday
import com.example.miqatapp.resources.tuesday
import com.example.miqatapp.resources.wednesday
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.StringResource

/** Localized weekday name for a date's day-of-week. */
val DayOfWeek.labelRes: StringResource
    get() = when (this) {
        DayOfWeek.MONDAY -> Res.string.monday
        DayOfWeek.TUESDAY -> Res.string.tuesday
        DayOfWeek.WEDNESDAY -> Res.string.wednesday
        DayOfWeek.THURSDAY -> Res.string.thursday
        DayOfWeek.FRIDAY -> Res.string.friday
        DayOfWeek.SATURDAY -> Res.string.saturday
        DayOfWeek.SUNDAY -> Res.string.sunday
    }
