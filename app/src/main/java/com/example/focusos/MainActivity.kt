package com.example.focusos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.focusos.presentation.home.HomeScreen
import com.example.focusos.presentation.settings.SettingsScreen
import com.example.focusos.ui.theme.FocusOsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var showSettings by remember { mutableStateOf(false) }
            FocusOsTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color.Black
                ) {
                    if (showSettings) {
                        SettingsScreen(onBack = { showSettings = false })
                    } else {
                        HomeScreen(onOpenSettings = { showSettings = true })
                    }
                }
            }
        }
    }
}

