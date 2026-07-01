package com.example.miqatapp.core.platform

/**
 * Whether the app can drive the system's Do-Not-Disturb / silent state.
 * Android: yes (with the DND-access permission). iOS: no — Apple forbids third-party apps from
 * toggling system DND/Focus, so the Prayer Focus feature is hidden there.
 */
expect val canControlDnd: Boolean
