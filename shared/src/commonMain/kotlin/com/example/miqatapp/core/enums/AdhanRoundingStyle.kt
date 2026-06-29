package com.example.miqatapp.core.enums

/** How computed prayer times are rounded. */
enum class AdhanRoundingStyle(val label: String) {
    Nearest("Nearest minute"),
    Up("Round up"),
    None("No rounding"),
}
