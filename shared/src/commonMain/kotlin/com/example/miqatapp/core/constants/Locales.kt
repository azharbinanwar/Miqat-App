package com.example.miqatapp.core.constants

/** A supported app language. */
data class AppLocale(val code: String, val name: String, val isRtl: Boolean)

/** Supported locales + default. Add a language → add one line here. */
object Locales {
    val English = AppLocale("en", "English", isRtl = false)
    val Arabic = AppLocale("ar", "العربية", isRtl = true)
    val Urdu = AppLocale("ur", "اردو", isRtl = true)

    val supported = listOf(English, Arabic, Urdu)
    val default = English

    fun byCode(code: String): AppLocale = supported.firstOrNull { it.code == code } ?: default
}
