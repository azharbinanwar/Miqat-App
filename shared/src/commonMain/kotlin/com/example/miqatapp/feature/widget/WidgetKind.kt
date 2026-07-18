package com.example.miqatapp.feature.widget

/** The widgets the gallery lists. cols×rows is the home-screen span, used to size the in-app preview. */
enum class WidgetKind(val label: String, val cols: Int, val rows: Int) {
    Times("Prayer Times", 4, 1),
    Bar("Prayer Bar", 4, 1),
    Card("Prayer Card", 4, 2),
    Minimal("Prayer Minimal", 2, 2),
    Current("Current Prayer", 2, 2),
    Tile("Prayer Tile", 2, 2),
    Icon("Prayer Icon", 1, 1),
}

/** Ask the launcher to pin [kind] to the home screen. No-op where pinning isn't supported. */
expect fun pinWidget(kind: WidgetKind)
