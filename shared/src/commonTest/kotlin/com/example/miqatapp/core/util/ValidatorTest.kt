package com.example.miqatapp.core.util

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ValidatorTest {
    @Test fun required_catchesBlank() {
        assertNotNull(Validator.required(""))
        assertNotNull(Validator.required("  "))
        assertNull(Validator.required("ok"))
    }

    @Test fun latLng_rangeChecks() {
        assertNull(Validator.latitude("21.4225"))
        assertNotNull(Validator.latitude("91"))
        assertNull(Validator.longitude("39.8262"))
        assertNotNull(Validator.longitude("181"))
        assertNotNull(Validator.longitude("abc"))
    }
}
