package com.example.focusos.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focusos.domain.model.AppItem

@Composable
fun LockdownOverlay(
    remainingTimeMs: Long,
    safeApps: List<AppItem>,
    onAppClick: (AppItem) -> Unit,
    onEmergencyOverrideClick: () -> Unit
) {
    val minutes = remainingTimeMs / 1000 / 60
    val seconds = (remainingTimeMs / 1000) % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    BackHandler(enabled = true) {

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier.height(48.dp)
        )

        Text(
            text = "SYSTEM LOCKDOWN",
            color = Color.Red,
            fontSize = 24.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Anxiety threshold exceeded.\nDopamine triggers disabled.",
            color = Color.Gray,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = timeString,
            color = Color.White,
            fontSize = 48.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "--- UTILITIES ONLY ---",
                        color = Color.DarkGray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                items(safeApps) { app ->
                    AppItemView(
                        label = app.label,
                        onClick = { onAppClick(app) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        TextButton(onClick = onEmergencyOverrideClick) {
            Text(
                text = "EMERGENCY OVERRIDE",
                color = Color.DarkGray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }
    }

}