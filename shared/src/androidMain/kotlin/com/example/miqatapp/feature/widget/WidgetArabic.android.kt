package com.example.miqatapp.feature.widget

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.util.Locale

actual fun arabicLabel(res: StringResource): String {
    val prev = Locale.getDefault()
    return try {
        Locale.setDefault(Locale("ar"))
        runBlocking { getString(res) }
    } finally {
        Locale.setDefault(prev)
    }
}
