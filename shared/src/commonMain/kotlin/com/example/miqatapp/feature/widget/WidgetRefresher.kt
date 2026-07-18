package com.example.miqatapp.feature.widget

/** Redraw the on-screen widget(s) and re-arm the transition alarms. Reads the just-written [WidgetStore]. No-op on iOS for now. */
expect object WidgetRefresher {
    fun refresh()

    /** Just repaint the widgets (no alarm re-arm, no preview push). For instant look changes from the editor. */
    fun redraw()
}
