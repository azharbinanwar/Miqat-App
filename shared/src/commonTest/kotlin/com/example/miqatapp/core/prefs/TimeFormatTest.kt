package com.example.miqatapp.core.prefs

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatTest {
    @Test fun twentyFour_padsHourAndMinute() {
        assertEquals("05:12", TimeFormat.TwentyFour.format(5 * 60 + 12))
        assertEquals("00:00", TimeFormat.TwentyFour.format(0))
        assertEquals("23:59", TimeFormat.TwentyFour.format(23 * 60 + 59))
    }

    @Test fun twelve_handlesNoonMidnightBoundaries() {
        assertEquals("12:00 AM", TimeFormat.Twelve.format(0))          // midnight
        assertEquals("5:12 AM", TimeFormat.Twelve.format(5 * 60 + 12))
        assertEquals("12:21 PM", TimeFormat.Twelve.format(12 * 60 + 21)) // noon
        assertEquals("1:00 PM", TimeFormat.Twelve.format(13 * 60))      // 13→1
        assertEquals("11:59 PM", TimeFormat.Twelve.format(23 * 60 + 59))
    }
}
