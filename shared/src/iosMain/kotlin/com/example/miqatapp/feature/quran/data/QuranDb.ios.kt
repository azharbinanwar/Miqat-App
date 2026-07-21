package com.example.miqatapp.feature.quran.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile

// write once into the Documents dir; delete the file to force a refresh when a new db ships
@OptIn(ExperimentalForeignApi::class)
actual fun materializeDb(name: String, bytes: ByteArray): String {
    val dir = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String
    val path = "$dir/$name"
    if (!NSFileManager.defaultManager.fileExistsAtPath(path)) {
        val data = bytes.usePinned { NSData.create(bytes = it.addressOf(0), length = bytes.size.toULong()) }
        data.writeToFile(path, atomically = true)
    }
    return path
}
