package com.example.miqatapp.feature.quran.data

import com.example.miqatapp.core.platform.AppCtx
import java.io.File

// rewrite only when missing or a different size (so a shipped db update takes effect, otherwise cheap no-op)
actual fun materializeDb(name: String, bytes: ByteArray): String {
    val f = File(AppCtx.context.filesDir, name)
    if (!f.exists() || f.length() != bytes.size.toLong()) f.writeBytes(bytes)
    return f.absolutePath
}
