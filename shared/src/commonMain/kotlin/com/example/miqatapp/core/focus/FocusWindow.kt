package com.example.miqatapp.core.focus

// A concrete silent window, resolved from a prayer time + its offsets. Label is the prayer name.
data class FocusWindow(val label: String, val mode: SilenceMode, val startMillis: Long, val endMillis: Long)
