package com.example.miqatapp.feature.quran.data

/** Copy a bundled db asset (bytes) to a writable file path and return it — SQLite opens a file, not a resource. */
expect fun materializeDb(name: String, bytes: ByteArray): String
