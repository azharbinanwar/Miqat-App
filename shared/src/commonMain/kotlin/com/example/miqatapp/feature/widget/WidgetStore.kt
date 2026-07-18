package com.example.miqatapp.feature.widget

/** The plain widget-readable store — a single JSON string. Android: SharedPreferences; iOS later: App Group. */
expect object WidgetStore {
    fun write(json: String)
    fun read(): String?
}
