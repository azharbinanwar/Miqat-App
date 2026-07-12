package com.example.miqatapp.core.enums

import androidx.compose.runtime.Composable
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.time_12_hour
import com.example.miqatapp.resources.time_24_hour
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** 12h / 24h clock. [pattern] feeds LocalTime/LocalDateTime.format(pattern) — the user's choice, reactive via SettingsStore. */
enum class TimeFormat(private val labelRes: StringResource, val pattern: String) {
    Twelve(Res.string.time_12_hour, "h:mm a"),
    TwentyFour(Res.string.time_24_hour, "HH:mm"),
    ;

    val value: String get() = name

    @Composable
    fun label(): String = stringResource(labelRes)

    companion object {
        val default = Twelve
        fun fromValue(value: String?) = entries.firstOrNull { it.value == value } ?: default
    }
}
