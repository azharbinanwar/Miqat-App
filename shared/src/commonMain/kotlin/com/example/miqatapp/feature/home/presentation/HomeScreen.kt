package com.example.miqatapp.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miqatapp.core.navigation.AppRoute
import com.example.miqatapp.core.navigation.LocalNavController
import com.example.miqatapp.core.widgets.AppButton

@Composable
fun HomeScreen() {
    val nav = LocalNavController.current
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Miqat — Home")
            AppButton(text = "Open Sandbox", onClick = { nav.navigate(AppRoute.Sandbox) })
        }
    }
}
