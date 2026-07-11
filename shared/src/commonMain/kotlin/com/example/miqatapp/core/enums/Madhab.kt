package com.example.miqatapp.core.enums

/** Asr calculation school. */
enum class Madhab(val label: String) {
    Hanafi("Hanafi"),
    Shafi("Shafi'i"),
    ;

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name }
    }
}
