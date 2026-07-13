package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate

actual fun currentDate(): LocalDate {
    val comps = NSCalendar.currentCalendar.components(
        NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
        NSDate(),
    )
    return LocalDate(comps.year.toInt(), comps.month.toInt(), comps.day.toInt())
}

actual fun currentTime(): LocalTime {
    val comps = NSCalendar.currentCalendar.components(
        NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
        NSDate(),
    )
    return LocalTime(comps.hour.toInt(), comps.minute.toInt(), comps.second.toInt())
}
