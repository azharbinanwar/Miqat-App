package com.example.miqatapp.core.focus

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Vibrate
import com.composables.icons.lucide.VolumeX
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.silence_silent
import com.example.miqatapp.resources.silence_vibrate
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// How the phone is quieted during a prayer window. Silent needs Do Not Disturb access; Vibrate doesn't.
enum class SilenceMode(val icon: ImageVector, val labelRes: StringResource) {
    Silent(Lucide.VolumeX, Res.string.silence_silent),
    Vibrate(Lucide.Vibrate, Res.string.silence_vibrate);

    @Composable
    fun label(): String = stringResource(labelRes)
}
