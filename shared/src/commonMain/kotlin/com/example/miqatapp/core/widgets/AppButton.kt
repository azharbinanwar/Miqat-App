package com.example.miqatapp.core.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme

/** Visual style of the button. Same vocabulary as the Flutter reference. */
enum class AppButtonVariant { Primary, Outline, Elevator, Text, Error, ErrorOutline }

/** Size presets — height / font / corner radius / horizontal padding in one place. */
enum class AppButtonSize(
    val height: Dp,
    val fontSize: TextUnit,
    val cornerRadius: Dp,
    val horizontalPadding: Dp,
) {
    Large(50.dp, 16.sp, 14.dp, 24.dp),
    Medium(44.dp, 15.sp, 12.dp, 20.dp),
    Small(36.dp, 13.sp, 10.dp, 16.dp),
}

/**
 * One button for the whole app. Variant + size are parameters with defaults,
 * so calls read like Flutter named constructors but there's one body to maintain.
 *
 *   AppButton(text = "Save", onClick = …)                                  // Primary, Large
 *   AppButton(text = "Cancel", variant = AppButtonVariant.Outline, …)
 *   AppButton(text = "Delete", variant = AppButtonVariant.Error, size = AppButtonSize.Small, …)
 *
 * Disabled state is handled by M3 ButtonDefaults (theme-correct, no hardcoded grey).
 * `isProcessing` swaps the label for a spinner and blocks taps.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    size: AppButtonSize = AppButtonSize.Large,
    enabled: Boolean = true,
    isProcessing: Boolean = false,
    leftIcon: @Composable (() -> Unit)? = null,
    rightIcon: @Composable (() -> Unit)? = null,
) {
    val c = AppTheme.colors
    val shape = RoundedCornerShape(size.cornerRadius)
    val padding = PaddingValues(horizontal = size.horizontalPadding)
    val mod = modifier.height(size.height)
    val active = enabled && !isProcessing

    val content: @Composable () -> Unit = {
        AppButtonContent(text, size.fontSize, isProcessing, leftIcon, rightIcon)
    }

    when (variant) {
        AppButtonVariant.Primary -> Button(
            onClick, mod, active, shape = shape, contentPadding = padding,
            colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
        ) { content() }

        AppButtonVariant.Error -> Button(
            onClick, mod, active, shape = shape, contentPadding = padding,
            colors = ButtonDefaults.buttonColors(containerColor = c.error, contentColor = c.onError),
        ) { content() }

        AppButtonVariant.Elevator -> Button(
            onClick, mod, active, shape = shape, contentPadding = padding,
            colors = ButtonDefaults.buttonColors(containerColor = c.cardColor, contentColor = c.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        ) { content() }

        AppButtonVariant.Outline -> OutlinedButton(
            onClick, mod, active, shape = shape, contentPadding = padding,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = c.primary),
            border = BorderStroke(1.dp, if (active) c.primary else c.neutralVariant),
        ) { content() }

        AppButtonVariant.ErrorOutline -> OutlinedButton(
            onClick, mod, active, shape = shape, contentPadding = padding,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = c.error),
            border = BorderStroke(1.dp, if (active) c.error else c.neutralVariant),
        ) { content() }

        AppButtonVariant.Text -> TextButton(
            onClick, mod, active, shape = shape, contentPadding = padding,
            colors = ButtonDefaults.textButtonColors(contentColor = c.primary),
        ) { content() }
    }
}

/** Spinner-or-(icon+label+icon). Built once; spinner inherits the button's content color. */
@Composable
private fun AppButtonContent(
    text: String,
    fontSize: TextUnit,
    isProcessing: Boolean,
    leftIcon: @Composable (() -> Unit)?,
    rightIcon: @Composable (() -> Unit)?,
) {
    if (isProcessing) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current,
        )
        return
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leftIcon?.invoke()
        Text(text = text, fontSize = fontSize, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        rightIcon?.invoke()
    }
}
