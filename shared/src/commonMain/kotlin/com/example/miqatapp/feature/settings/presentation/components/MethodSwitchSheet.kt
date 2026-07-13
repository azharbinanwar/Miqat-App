package com.example.miqatapp.feature.settings.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.AppButtonVariant
import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.constants.countryLabel
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.keep_current_method
import com.example.miqatapp.resources.switch_method_body
import com.example.miqatapp.resources.switch_method_title
import com.example.miqatapp.resources.switch_to_method
import org.jetbrains.compose.resources.stringResource

/** After a manual location pick, offers the new country's [method]. Location is already set; skip keeps the current method. */
@Composable
fun MethodSwitchSheet(
    place: Place,
    method: CalculationMethod,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    val placeText = "${place.name}, ${place.countryLabel}"
    AppBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(Res.string.switch_method_title),
        footer = {
            AppButton(stringResource(Res.string.switch_to_method, method.shortName), onClick = onConfirm, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.keep_current_method), onClick = onDismiss, variant = AppButtonVariant.Text, modifier = Modifier.fillMaxWidth())
        },
    ) {
        val body = stringResource(Res.string.switch_method_body, placeText, method.label)
        // bold only the dynamic values, so RTL word order stays correct
        Text(boldParts(body, listOf(placeText, method.label), c.onSurface), fontSize = 14.sp, color = c.onSurfaceVariant)
    }
}

/** Bold each of [parts] wherever it appears in [text]. */
private fun boldParts(text: String, parts: List<String>, color: Color): AnnotatedString = buildAnnotatedString {
    append(text)
    for (p in parts) {
        var i = text.indexOf(p)
        while (i >= 0) {
            addStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = color), i, i + p.length)
            i = text.indexOf(p, i + p.length)
        }
    }
}
