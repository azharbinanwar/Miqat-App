package com.example.miqatapp.feature.settings.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Settings
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.widgets.LocalDrawerState
import com.example.miqatapp.core.widgets.StateView
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.coming_soon
import com.example.miqatapp.resources.settings
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = AppTheme.colors.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.scaffoldBackgroundColor,
                    titleContentColor = AppTheme.colors.onSurface,
                    navigationIconContentColor = AppTheme.colors.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
            StateView(
                title = stringResource(Res.string.coming_soon),
                icon = { Icon(Lucide.Settings, null, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(56.dp)) },
            )
        }
    }
}
