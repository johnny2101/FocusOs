package com.example.focusos.presentation.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.focusos.presentation.components.EmergencyOverrideDialog
import com.example.focusos.presentation.components.FrictionDialog
import com.example.focusos.presentation.components.ServiceWarningBanner

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit,
    onOpenAnalytics: () -> Unit
) {
    val apps by viewModel.appList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val frictionPackage by viewModel.frictionState.collectAsState()
    val lockdownRemainingTime by viewModel.lockdownRemainingTime.collectAsState()
    val isLockdownActive = lockdownRemainingTime > 0
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAccessibilityRevoked by viewModel.isAccessibilityRevoked.collectAsState()
    val showEmergencyDialog by viewModel.showEmergencyDialog.collectAsState()

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onSearchQueryChange("")
                focusManager.clearFocus()
                viewModel.checkServiceStatus()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        if (isLockdownActive) {
            LockdownOverlay(
                remainingTimeMs = lockdownRemainingTime,
                safeApps = apps,
                onAppClick = { viewModel.onAppClick(it) },
                onEmergencyOverrideClick = viewModel::onEmergencyOverrideRequested
            )

            if (showEmergencyDialog) {
                EmergencyOverrideDialog(
                    onDismiss = viewModel::onEmergencyOverrideDismissed,
                    onSuccess = viewModel::onEmergencyOverrideConfirmed
                )
            }
        } else {
            Column {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "GIT LOG",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable { onOpenAnalytics() } // Pass this callback from MainActivity
                        )
                        Text(
                            text = "CONFIG",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.clickable { onOpenSettings() }
                        )
                    }
                }

                if (isAccessibilityRevoked) {
                    ServiceWarningBanner(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                }

                if (isLockdownActive) {
                    val minutes = lockdownRemainingTime / 1000 / 60
                    val seconds = (lockdownRemainingTime / 1000) % 60
                    val timeString = String.format(
                        "%02d:%02d",
                        minutes,
                        seconds
                    )

                    Text(
                        text = "Focus restoring in $timeString",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Red)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            item {
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = viewModel::onSearchQueryChange,
                                    textStyle = TextStyle(
                                        color = Color.Green,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(Color.Green),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    decorationBox = { innerTextField ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "> ",
                                                color = Color.Gray,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Box {
                                                if (searchQuery.isEmpty()) {
                                                    Text(
                                                        text = "find_app...",
                                                        color = Color.DarkGray,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        }
                                    }
                                )
                            }
                            items(apps) { app ->
                                AppItemView(
                                    label = app.label,
                                    onClick = {
                                        viewModel.onAppClick(app)
                                    }
                                )
                            }
                        }

                        frictionPackage?.let { appItem ->
                            FrictionDialog(
                                packageName = appItem.packageName,
                                onDismiss = viewModel::onFrictionDismissed,
                                onSuccess = viewModel::onFrictionPassed
                            )
                        }
                    }
                }
            }
        }
    }

}

@Composable
fun AppItemView(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp)
    )
}