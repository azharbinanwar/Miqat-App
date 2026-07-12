package com.example.miqatapp.core.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.LayoutDirection
import com.example.miqatapp.core.store.SettingsStore

/**
 * Supported UI languages. The [label] stays in its own script (not translated — you always see "English"
 * and "العربية"). Add a language here and the Settings picker reflects it. [code] is stored in prefs;
 * [direction] drives the app's LTR/RTL layout — no per-language checks at the call site.
 */
enum class Language(val label: String, val code: String, val direction: LayoutDirection) {
    English("English", "en", LayoutDirection.Ltr),
    Arabic("العربية", "ar", LayoutDirection.Rtl),
    // later: Urdu("اردو", "ur", LayoutDirection.Rtl), …
    ;

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: English

        /** The language in effect right now (reactive — recomposes on switch). */
        val current: Language
            @Composable get() = SettingsStore.language.collectAsState().value
    }
}

/**
 * Pick an English vs Arabic value for the current language — the KMP take on Flutter's `getTr(en, ar)`.
 * Type-agnostic: strings, icons, alignments, whatever differs by locale. Since Arabic is the only RTL
 * language, this doubles as an LTR/RTL switch (`tr(startIcon, endIcon)`).
 */
@Composable
fun <T> tr(en: T, ar: T): T = if (Language.current == Language.Arabic) ar else en
