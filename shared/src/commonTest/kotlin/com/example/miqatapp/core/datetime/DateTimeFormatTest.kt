package com.example.miqatapp.core.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeFormatTest {
    private val pm = LocalDateTime(2026, 7, 12, 17, 8, 42)   // 17:08:42
    private val am = LocalTime(0, 5)                          // midnight edge

    @Test
    fun patternDrivesOutput() {
        assertEquals("08", pm.format("mm"))
        assertEquals("17:08", pm.format("HH:mm"))
        assertEquals("17:08:42", pm.format("HH:mm:ss"))
        assertEquals("12/07/2026", pm.format("dd/MM/yyyy"))
        assertEquals("2026-07-12", pm.format("yyyy-MM-dd"))
        assertEquals("12/07/2026 17:08", pm.format("dd/MM/yyyy HH:mm"))
    }

    @Test
    fun twelveHourAmPm() {
        assertEquals("5:08 PM", pm.format("h:mm a"))
        assertEquals("05:08 PM", pm.format("hh:mm a"))
        assertEquals("12:05 AM", am.format("h:mm a"))                  // 0h -> 12 AM
        assertEquals("12:00 PM", LocalTime(12, 0).format("h:mm a"))    // noon -> 12 PM
        assertEquals("11:59 PM", LocalTime(23, 59).format("h:mm a"))
    }

    @Test
    fun paddingAndDateOverload() {
        val d = LocalDate(2026, 3, 5)
        assertEquals("05/03/2026", d.format("dd/MM/yyyy"))   // single digits zero-padded
        assertEquals("2026-3-5", d.format("yyyy-M-d"))       // unpadded when single letter
    }
}
