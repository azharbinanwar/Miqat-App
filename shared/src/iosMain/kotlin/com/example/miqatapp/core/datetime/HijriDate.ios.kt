package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSCalendarIdentifierIslamicUmmAlQura
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents

actual fun toHijri(date: LocalDate): HijriDate {
    // build an NSDate for the Gregorian date, then read Islamic (Umm al-Qura) components off it
    val gregorian = NSCalendar(calendarIdentifier = NSCalendarIdentifierGregorian)
    val comps = NSDateComponents().apply {
        year = date.year.toLong()
        month = date.month.number.toLong()
        day = date.day.toLong()
    }
    val nsDate = gregorian.dateFromComponents(comps) ?: NSDate()
    val islamic = NSCalendar(calendarIdentifier = NSCalendarIdentifierIslamicUmmAlQura)
    val h = islamic.components(NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay, nsDate)
    return HijriDate(day = h.day.toInt(), month = h.month.toInt(), year = h.year.toInt())
}
