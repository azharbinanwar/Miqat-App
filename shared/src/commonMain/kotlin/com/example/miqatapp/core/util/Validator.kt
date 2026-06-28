package com.example.miqatapp.core.util

/** Field validators — return an error message, or null if valid. (Miqat needs only these.) */
object Validator {
    fun required(value: String?, field: String = "This field"): String? =
        if (value.isNullOrBlank()) "$field is required" else null

    fun number(value: String?): String? =
        if (value?.trim()?.toDoubleOrNull() == null) "Enter a valid number" else null

    fun latitude(value: String?): String? {
        val v = value?.trim()?.toDoubleOrNull() ?: return "Enter a valid latitude"
        return if (v in -90.0..90.0) null else "Latitude must be between -90 and 90"
    }

    fun longitude(value: String?): String? {
        val v = value?.trim()?.toDoubleOrNull() ?: return "Enter a valid longitude"
        return if (v in -180.0..180.0) null else "Longitude must be between -180 and 180"
    }
}
