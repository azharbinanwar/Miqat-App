package com.example.miqatapp.feature.widget

import org.jetbrains.compose.resources.StringResource

// The ring shows the prayer name in Arabic regardless of app language. Resolve the SAME string resource
// (values-ar) under the Arabic locale — one source of truth, no hardcoded copy.
expect fun arabicLabel(res: StringResource): String
