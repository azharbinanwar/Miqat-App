package com.example.miqatapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform