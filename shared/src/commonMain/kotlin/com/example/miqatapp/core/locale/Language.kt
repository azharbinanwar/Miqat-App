package com.example.miqatapp.core.locale

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.LayoutDirection
import com.example.miqatapp.core.prefs.LabeledOption
import com.example.miqatapp.core.prefs.Prefs

/**
 * Supported UI languages. The label stays in its own script (not translated — you always see "English"
 * and "العربية"). Add a language here and the Settings picker reflects it. [code] is stored in prefs;
 * [direction] drives the app's LTR/RTL layout — no per-language checks at the call site.
 */
enum class Language(private val text: String, val code: String, val direction: LayoutDirection) : LabeledOption {
    English("English", "en", LayoutDirection.Ltr),
    Arabic("العربية", "ar", LayoutDirection.Rtl),
    // later: Urdu("اردو", "ur", LayoutDirection.Rtl), …
    ;

    override val value: String get() = code

    @Composable
    override fun label(): String = text

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: English

        /** The language in effect right now (reactive — recomposes on switch). */
        val current: Language
            @Composable get() = fromCode(Prefs.language)
    }
}

/**
 * Pick an English vs Arabic value for the current language — the KMP take on Flutter's `getTr(en, ar)`.
 * Type-agnostic: strings, icons, alignments, whatever differs by locale. Since Arabic is the only RTL
 * language, this doubles as an LTR/RTL switch (`tr(startIcon, endIcon)`).
 */
@Composable
fun <T> tr(en: T, ar: T): T = if (Language.current == Language.Arabic) ar else en
