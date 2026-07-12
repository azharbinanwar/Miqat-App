package com.example.miqatapp.core.enums

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatTest {
    @Test fun eachChoiceCarriesItsPattern() {
        assertEquals("HH:mm", TimeFormat.TwentyFour.pattern)   // rendering itself is covered by DateTimeFormatTest
        assertEquals("h:mm a", TimeFormat.Twelve.pattern)
    }

    @Test fun fromValueRoundTripsAndDefaults() {
        assertEquals(TimeFormat.Twelve, TimeFormat.fromValue("Twelve"))
        assertEquals(TimeFormat.TwentyFour, TimeFormat.fromValue("TwentyFour"))
        assertEquals(TimeFormat.default, TimeFormat.fromValue(null))
        assertEquals(TimeFormat.default, TimeFormat.fromValue("garbage"))
    }
}
