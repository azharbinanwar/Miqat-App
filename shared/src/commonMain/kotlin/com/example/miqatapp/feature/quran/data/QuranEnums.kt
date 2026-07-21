package com.example.miqatapp.feature.quran.data

import androidx.compose.runtime.Composable
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.revelation_meccan
import com.example.miqatapp.resources.revelation_medinan
import com.example.miqatapp.resources.sajda_obligatory
import com.example.miqatapp.resources.sajda_recommended
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Where a surah was revealed. Same shape as [com.example.miqatapp.core.enums.Miqat]: carries its own
 *  localized label so screens never hardcode the text. */
enum class Revelation(val labelRes: StringResource) {
    Meccan(Res.string.revelation_meccan),
    Medinan(Res.string.revelation_medinan);

    val label: String @Composable get() = stringResource(labelRes)
}

/** A prostration (sajda) verse — obligatory or recommended. */
enum class Sajda(val labelRes: StringResource) {
    Recommended(Res.string.sajda_recommended),
    Obligatory(Res.string.sajda_obligatory);

    val label: String @Composable get() = stringResource(labelRes)
}
