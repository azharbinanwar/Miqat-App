package com.example.miqatapp.feature.qibla.store

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.enums.QiblaStyle
import com.example.miqatapp.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Remembers the compass dial style the user last picked. Reads Prefs once to seed, then the setter
 * persists + emits so the Qibla screen updates and the choice sticks across launches.
 */
object QiblaStyleStore {

    private val _style = MutableStateFlow(
        QiblaStyle.fromName(PrefsService.getStringOrNull(PrefConst.QIBLA_STYLE)) ?: QiblaStyle.Modern,
    )
    val style: StateFlow<QiblaStyle> = _style.asStateFlow()

    fun setStyle(value: QiblaStyle) {
        PrefsService.putString(PrefConst.QIBLA_STYLE, value.name)
        _style.value = value
    }
}
