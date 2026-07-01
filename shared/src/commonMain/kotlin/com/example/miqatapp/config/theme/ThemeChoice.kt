package com.example.miqatapp.config.theme

import androidx.compose.runtime.Composable
import com.example.miqatapp.core.prefs.LabeledOption
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.dark
import com.example.miqatapp.resources.light
import com.example.miqatapp.resources.system
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** App appearance options. [dark] null = follow the system. */
enum class ThemeChoice(private val labelRes: StringResource, val dark: Boolean?) : LabeledOption {
    Light(Res.string.light, dark = false),
    Dark(Res.string.dark, dark = true),
    System(Res.string.system, dark = null),
    ;

    override val value: String get() = name

    @Composable
    override fun label(): String = stringResource(labelRes)

    companion object {
        val default = System
        fun fromValue(value: String?) = entries.firstOrNull { it.value == value } ?: default
    }
}
