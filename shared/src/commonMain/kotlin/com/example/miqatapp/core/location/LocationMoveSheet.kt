package com.example.miqatapp.core.location

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.AppButtonVariant
import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.constants.countryLabel
import kotlinx.coroutines.launch

/**
 * Shown on launch when GPS says you've moved to a new city. Quiet by default — the city and (only when
 * the method would change) an optional switch, its reason tucked behind an info button.
 * ponytail: strings inline; move to resources with the rest.
 */
@Composable
fun LocationMoveSheet(
    candidate: Place,
    current: Place,
    methodChange: Pair<CalculationMethod, CalculationMethod>?,   // old → new; null when the method won't change
    onUpdate: (switchMethod: Boolean) -> Unit,
    onKeep: () -> Unit,
) {
    val c = AppTheme.colors
    var switchMethod by remember { mutableStateOf(true) }

    AppBottomSheet(
        onDismiss = onKeep,
        title = "Location changed",
        subtitle = "Update prayer times for your new location?",
        footer = {
            AppButton("Update", onClick = { onUpdate(methodChange != null && switchMethod) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton("Keep ${current.name}", onClick = onKeep, variant = AppButtonVariant.Text, modifier = Modifier.fillMaxWidth())
        },
    ) {
        // the new city
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(c.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Lucide.MapPin, null, tint = c.primary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(candidate.name, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = c.onSurface)
                Text(candidate.countryLabel, fontSize = 13.sp, color = c.onSurfaceVariant)
            }
        }

        // optional method switch — only when the method would actually change
        if (methodChange != null) {
            Spacer(Modifier.height(18.dp))
            MethodOptIn(
                methodName = methodChange.second.shortName,
                checked = switchMethod,
                onToggle = { switchMethod = !switchMethod },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodOptIn(methodName: String, checked: Boolean, onToggle: () -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    val tooltip = rememberTooltipState(isPersistent = true)

    Row(
        Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // compact, modern checkbox
        Box(
            Modifier.size(20.dp).clip(RoundedCornerShape(6.dp))
                .background(if (checked) c.primary else Color.Transparent)
                .border(1.5.dp, if (checked) c.primary else c.outlineVariant, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(Lucide.Check, null, tint = c.onPrimary, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            buildAnnotatedString {
                append("Also switch to ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(methodName) }
            },
            fontSize = 14.sp,
            color = c.onSurface,
            modifier = Modifier.weight(1f),
        )
        TooltipBox(
            positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
            tooltip = {
                RichTooltip {
                    Text("$methodName is the official calculation method where you are now. Different regions use different methods, so a few prayer times shift slightly.")
                }
            },
            state = tooltip,
        ) {
            IconButton(onClick = { scope.launch { tooltip.show() } }) {
                Icon(Lucide.Info, "Why switch method?", tint = c.onSurfaceVariant, modifier = Modifier.size(19.dp))
            }
        }
    }
}
