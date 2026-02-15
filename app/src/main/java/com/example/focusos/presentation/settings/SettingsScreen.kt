package com.example.focusos.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.focusos.domain.model.AppTier

@Composable
fun SettingScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val apps by viewModel.apps.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "< Back",
                color = Color.Gray,
                modifier = Modifier.clickable { onBack() }.padding(8.dp),
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "APP TIERS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        LazyColumn() {
            items(apps) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.cycleTier(app) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = app.label,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f),
                        fontFamily = FontFamily.Monospace
                    )

                    // Tier Badge
                    val tierColor = when (app.tier) {
                        AppTier.UTILITY -> Color.Green
                        AppTier.STANDARD -> Color.Gray
                        AppTier.DOPAMINE -> Color.Red
                    }

                    Text(
                        text = app.tier.name,
                        color = tierColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            }
        }
    }
}