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
import androidx.compose.material3.TooltipAnchorPosition
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
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.also_switch_to
import com.example.miqatapp.resources.keep_place
import com.example.miqatapp.resources.location_changed
import com.example.miqatapp.resources.method_official_explainer
import com.example.miqatapp.resources.update
import com.example.miqatapp.resources.update_prayer_times_q
import com.example.miqatapp.resources.why_switch_method
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch

/** Shown when GPS says you've moved — the new city + an optional method switch. */
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
        title = stringResource(Res.string.location_changed),
        subtitle = stringResource(Res.string.update_prayer_times_q),
        footer = {
            AppButton(stringResource(Res.string.update), onClick = { onUpdate(methodChange != null && switchMethod) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.keep_place, current.name), onClick = onKeep, variant = AppButtonVariant.Text, modifier = Modifier.fillMaxWidth())
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
        val alsoSwitch = stringResource(Res.string.also_switch_to, methodName)
        Text(
            buildAnnotatedString {
                append(alsoSwitch)
                val i = alsoSwitch.indexOf(methodName)
                if (i >= 0) addStyle(SpanStyle(fontWeight = FontWeight.Bold), i, i + methodName.length)
            },
            fontSize = 14.sp,
            color = c.onSurface,
            modifier = Modifier.weight(1f),
        )
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = {
                RichTooltip {
                    Text(stringResource(Res.string.method_official_explainer, methodName))
                }
            },
            state = tooltip,
        ) {
            IconButton(onClick = { scope.launch { tooltip.show() } }) {
                Icon(Lucide.Info, stringResource(Res.string.why_switch_method), tint = c.onSurfaceVariant, modifier = Modifier.size(19.dp))
            }
        }
    }
}
