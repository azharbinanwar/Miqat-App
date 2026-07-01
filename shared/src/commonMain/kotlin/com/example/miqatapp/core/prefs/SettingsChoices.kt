package com.example.miqatapp.core.prefs

import androidx.compose.runtime.Composable
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.time_12_hour
import com.example.miqatapp.resources.time_24_hour
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * A pickable option from a fixed set. Fixed sets are enums implementing this, so the picker is
 * type-safe/exhaustive and each option carries its own id + label. [value] is stored/compared;
 * [label] is what's shown (localized via `stringResource`, or a literal like a language name).
 */
interface LabeledOption {
    val value: String

    @Composable
    fun label(): String
}

/** 12h / 24h clock. */
enum class TimeFormat(private val labelRes: StringResource) : LabeledOption {
    Twelve(Res.string.time_12_hour),
    TwentyFour(Res.string.time_24_hour),
    ;

    override val value: String get() = name

    @Composable
    override fun label(): String = stringResource(labelRes)

    /** Render minutes-since-midnight as a clock string in this format ("5:12 AM" / "17:12"). */
    fun format(minutesOfDay: Int): String {
        val h24 = (minutesOfDay / 60).mod(24)
        val mm = minutesOfDay.mod(60).toString().padStart(2, '0')
        return when (this) {
            TwentyFour -> "${h24.toString().padStart(2, '0')}:$mm"
            Twelve -> {
                val h12 = ((h24 + 11) % 12) + 1          // 0→12, 13→1, 12→12
                "$h12:$mm ${if (h24 < 12) "AM" else "PM"}"
            }
        }
    }

    companion object {
        val default = Twelve
        fun fromValue(value: String?) = entries.firstOrNull { it.value == value } ?: default
    }
}
