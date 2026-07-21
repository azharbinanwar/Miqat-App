package com.example.miqatapp.feature.widget

import org.jetbrains.compose.resources.StringResource

// ponytail: iOS widgets aren't built yet (App Group store + WidgetKit come later). These no-op
// actuals just let the iOS target compile; the whole widget path is inert on iOS.

actual object WidgetStore {
    actual fun write(json: String) {}
    actual fun read(): String? = null
}

actual object WidgetRefresher {
    actual fun refresh() {}
    actual fun redraw() {}
}

actual fun pinWidget(kind: WidgetKind) {}

// Never reached on iOS (refresh is a no-op, so WidgetPublisher never runs). Empty until iOS widgets land.
actual fun arabicLabel(res: StringResource): String = ""
