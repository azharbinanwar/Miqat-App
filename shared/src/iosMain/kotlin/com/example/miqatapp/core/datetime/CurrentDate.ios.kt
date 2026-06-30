package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun currentDate(): LocalDate {
    val comps = NSCalendar.currentCalendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        NSDate(),
    )
    return LocalDate(comps.year.toInt(), comps.month.toInt(), comps.day.toInt())
}
