package com.example.miqatapp.feature.widget

import android.content.Context
import com.example.miqatapp.core.platform.AppCtx

actual object WidgetStore {
    private const val PREFS = "miqat_widget"
    private const val KEY = "snapshot"
    private fun prefs() = AppCtx.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    actual fun write(json: String) = prefs().edit().putString(KEY, json).apply()
    actual fun read(): String? = prefs().getString(KEY, null)
}
