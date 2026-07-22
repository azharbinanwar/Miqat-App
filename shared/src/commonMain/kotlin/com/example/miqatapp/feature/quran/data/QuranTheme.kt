package com.example.miqatapp.feature.quran.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.miqatapp.config.theme.AppColors
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.quran_theme_amber
import com.example.miqatapp.resources.quran_theme_charcoal
import com.example.miqatapp.resources.quran_theme_dark
import com.example.miqatapp.resources.quran_theme_dim_gray
import com.example.miqatapp.resources.quran_theme_dusty_blue
import com.example.miqatapp.resources.quran_theme_green
import com.example.miqatapp.resources.quran_theme_light
import com.example.miqatapp.resources.quran_theme_mint
import com.example.miqatapp.resources.quran_theme_night
import com.example.miqatapp.resources.quran_theme_paper
import com.example.miqatapp.resources.quran_theme_sepia
import com.example.miqatapp.resources.quran_theme_slate
import com.example.miqatapp.resources.quran_theme_system
import com.example.miqatapp.resources.quran_theme_warm_gray
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Reading themes for the Quran index + reader (Miqat-style). Each entry carries its own colors,
 * so we persist only the name and rebuild the palette on the screen — no color serialization.
 * [System] is the default and follows the app theme (its own colors below are ignored).
 * The colour options are tuned for long-reading comfort: soft, low-glare backgrounds, gentle contrast.
 */
enum class QuranTheme(
    val labelRes: StringResource,
    val background: Color,        // page
    val surface: Color,           // app bar / sheet base
    val surfaceContainer: Color,  // tiles, badges, number chips
    val onBackground: Color,      // ayah text
    val onSurface: Color,         // tile titles
    val onSurfaceVariant: Color,  // muted / subtitles
    val outline: Color,           // dividers, borders
    val primary: Color,           // accent (surah name, ع, markers)
    val onPrimary: Color,
) {
    // follows the app theme — colors here are placeholders, never applied (see applyTo / QuranThemeHost)
    System(
        Res.string.quran_theme_system,
        background = Color(0xFFFBF8F1), surface = Color(0xFFFFFFFF), surfaceContainer = Color(0xFFF2ECDF),
        onBackground = Color(0xFF2B2519), onSurface = Color(0xFF2B2519), onSurfaceVariant = Color(0xFF726852),
        outline = Color(0xFFDBD3C2), primary = Color(0xFF1E6B49), onPrimary = Color(0xFFFFFFFF),
    ),

    // ── light family (soft, low-glare) ──
    Light(
        Res.string.quran_theme_light,
        background = Color(0xFFFBF8F1), surface = Color(0xFFFFFFFF), surfaceContainer = Color(0xFFF2ECDF),
        onBackground = Color(0xFF2B2519), onSurface = Color(0xFF2B2519), onSurfaceVariant = Color(0xFF726852),
        outline = Color(0xFFDBD3C2), primary = Color(0xFF1E6B49), onPrimary = Color(0xFFFFFFFF),
    ),
    Paper( // warm off-white, softened contrast — least glare for long reading
        Res.string.quran_theme_paper,
        background = Color(0xFFF3ECDD), surface = Color(0xFFF9F3E7), surfaceContainer = Color(0xFFEBE2CE),
        onBackground = Color(0xFF3B342A), onSurface = Color(0xFF3B342A), onSurfaceVariant = Color(0xFF7C7263),
        outline = Color(0xFFDDD3BF), primary = Color(0xFF1E6B49), onPrimary = Color(0xFFFFFFFF),
    ),
    WarmGray( // neutral warm grey — soft, low glare
        Res.string.quran_theme_warm_gray,
        background = Color(0xFFECEAE5), surface = Color(0xFFF4F2EE), surfaceContainer = Color(0xFFE0DDD6),
        onBackground = Color(0xFF33312D), onSurface = Color(0xFF33312D), onSurfaceVariant = Color(0xFF77736C),
        outline = Color(0xFFD6D2CA), primary = Color(0xFF1E6B49), onPrimary = Color(0xFFFFFFFF),
    ),
    Amber( // warm amber, low blue-light — easy in the evening
        Res.string.quran_theme_amber,
        background = Color(0xFFFBEBCB), surface = Color(0xFFFDF3DD), surfaceContainer = Color(0xFFF4E1BC),
        onBackground = Color(0xFF4A3A1C), onSurface = Color(0xFF4A3A1C), onSurfaceVariant = Color(0xFF8C7440),
        outline = Color(0xFFE9D5A6), primary = Color(0xFF1E6B49), onPrimary = Color(0xFFFFFFFF),
    ),
    Sepia( // warm brown paper
        Res.string.quran_theme_sepia,
        background = Color(0xFFF1E4C9), surface = Color(0xFFF7EEDA), surfaceContainer = Color(0xFFEADCBB),
        onBackground = Color(0xFF4A3A20), onSurface = Color(0xFF4A3A20), onSurfaceVariant = Color(0xFF8A754C),
        outline = Color(0xFFDECFA8), primary = Color(0xFF1E6B49), onPrimary = Color(0xFFFFFFFF),
    ),
    DustyBlue( // cool desaturated blue — calm, low glare
        Res.string.quran_theme_dusty_blue,
        background = Color(0xFFE7ECF1), surface = Color(0xFFF1F4F8), surfaceContainer = Color(0xFFD9E1EA),
        onBackground = Color(0xFF26323F), onSurface = Color(0xFF26323F), onSurfaceVariant = Color(0xFF647485),
        outline = Color(0xFFC7D2DE), primary = Color(0xFF1E6B57), onPrimary = Color(0xFFFFFFFF),
    ),
    Green( // soft green tint
        Res.string.quran_theme_green,
        background = Color(0xFFE7F1E9), surface = Color(0xFFF1F7F2), surfaceContainer = Color(0xFFD9E9DC),
        onBackground = Color(0xFF20372A), onSurface = Color(0xFF20372A), onSurfaceVariant = Color(0xFF5E7566),
        outline = Color(0xFFC6DACB), primary = Color(0xFF1E6B49), onPrimary = Color(0xFFFFFFFF),
    ),
    Mint( // cool soft green-grey, calm
        Res.string.quran_theme_mint,
        background = Color(0xFFE9F0EE), surface = Color(0xFFF2F7F5), surfaceContainer = Color(0xFFDBE7E3),
        onBackground = Color(0xFF223531), onSurface = Color(0xFF223531), onSurfaceVariant = Color(0xFF5F736E),
        outline = Color(0xFFC7D8D3), primary = Color(0xFF1E6B57), onPrimary = Color(0xFFFFFFFF),
    ),

    // ── dark family (soft, not stark black) ──
    Dark(
        Res.string.quran_theme_dark,
        background = Color(0xFF171612), surface = Color(0xFF201E17), surfaceContainer = Color(0xFF272419),
        onBackground = Color(0xFFE9E3D5), onSurface = Color(0xFFE9E3D5), onSurfaceVariant = Color(0xFFA99E85),
        outline = Color(0xFF3A3527), primary = Color(0xFF5FB88D), onPrimary = Color(0xFF07130D),
    ),
    DimGray( // neutral dim grey — very low light, low glare
        Res.string.quran_theme_dim_gray,
        background = Color(0xFF1A1A19), surface = Color(0xFF242422), surfaceContainer = Color(0xFF2C2C2A),
        onBackground = Color(0xFFDDDBD5), onSurface = Color(0xFFDDDBD5), onSurfaceVariant = Color(0xFF97948C),
        outline = Color(0xFF383733), primary = Color(0xFF62BB91), onPrimary = Color(0xFF07130D),
    ),
    Charcoal( // soft warm dark grey — gentler than pure black
        Res.string.quran_theme_charcoal,
        background = Color(0xFF201F1D), surface = Color(0xFF2A2825), surfaceContainer = Color(0xFF33302B),
        onBackground = Color(0xFFE4DFD5), onSurface = Color(0xFFE4DFD5), onSurfaceVariant = Color(0xFFA39B8C),
        outline = Color(0xFF433E36), primary = Color(0xFF6BBF95), onPrimary = Color(0xFF08150E),
    ),
    Slate( // cool blue-grey dark, low glare
        Res.string.quran_theme_slate,
        background = Color(0xFF14181D), surface = Color(0xFF1C2127), surfaceContainer = Color(0xFF232A31),
        onBackground = Color(0xFFD3D8DE), onSurface = Color(0xFFD3D8DE), onSurfaceVariant = Color(0xFF8B95A0),
        outline = Color(0xFF2E353D), primary = Color(0xFF63B892), onPrimary = Color(0xFF07130D),
    ),
    Night( // near-black, dim — very low light
        Res.string.quran_theme_night,
        background = Color(0xFF0A0A0B), surface = Color(0xFF141416), surfaceContainer = Color(0xFF1B1B1E),
        onBackground = Color(0xFFB9B4A8), onSurface = Color(0xFFB9B4A8), onSurfaceVariant = Color(0xFF7C735D),
        outline = Color(0xFF2A2A2E), primary = Color(0xFF4FA97E), onPrimary = Color(0xFF06120C),
    );

    val label: String
        @Composable get() = stringResource(labelRes)

    companion object {
        val DEFAULT = System
    }
}

// map this reading theme's colors onto the full app palette. System follows the app (no override).
fun QuranTheme.applyTo(base: AppColors): AppColors =
    if (this == QuranTheme.System) base
    else base.copy(
        background = background, onBackground = onBackground,
        surface = surface, onSurface = onSurface,
        surfaceVariant = surfaceContainer, onSurfaceVariant = onSurfaceVariant,
        surfaceDim = background, surfaceBright = surface,
        surfaceContainerLowest = background, surfaceContainerLow = surface,
        surfaceContainer = surfaceContainer, surfaceContainerHigh = surfaceContainer, surfaceContainerHighest = surfaceContainer,
        outline = outline, outlineVariant = outline, surfaceTint = primary,
        primary = primary, onPrimary = onPrimary,
        scaffoldBackgroundColor = background, cardColor = surfaceContainer, canvasColor = background, appbarColor = surface,
    )
