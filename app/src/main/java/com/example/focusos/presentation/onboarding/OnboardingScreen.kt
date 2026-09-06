package com.example.focusos.presentation.onboarding

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onEvent(OnboardingEvent.CheckPermissions)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is OnboardingState.NavigatingToHome) {
            onOnboardingFinished()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Crossfade(targetState = uiState, label = "OnboaardingCrossfade") { state ->
            when (state) {
                is OnboardingState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }

                is OnboardingState.NeedsDefaultHome -> {
                    PermissionStep(
                        title = "Take Control",
                        description = "FocusOS must be your default home app to intercept doomscrolling and apply friction.",
                        buttonText = "Set Default Home",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                        }
                    )
                }

                is OnboardingState.NeedsUsageStats -> {
                    PermissionStep(
                        title = "Insight Over Guilt",
                        description = "We need Usage Access to generate your Git-style weekly reports and detect rapid app switching. \n\nPlease find 'FocusOS' in the list and allow access.",
                        buttonText = "Grant Usage Access",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }
                    )
                }

                is OnboardingState.NeedsNotification -> {
                    val launcher = rememberLauncherForActivityResult(
                        RequestPermission()
                    ) { isGranted ->
                        viewModel.onEvent(OnboardingEvent.CheckPermissions)
                    }

                    PermissionStep(
                        title = "The Conscience",
                        description = "FocusOS runs a lightweight background service to monitor your anxiety state. We need permission to show its persistent notification.",
                        buttonText = "Allow Notifications",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.onEvent(OnboardingEvent.CheckPermissions)
                            }
                        }
                    )
                }

                is OnboardingState.NeedsAccessibility -> {
                    val launcher = rememberLauncherForActivityResult(
                        RequestPermission()
                    ) { isGranted ->
                        viewModel.onEvent(OnboardingEvent.CheckPermissions)
                    }



                    PermissionStep(
                        title = "The Gatekeeper",
                        description = "FocusOS needs Accessibility access to instantly detect when you open a high-dopamine app. This uses zero battery in the background.\n\nPlease find 'FocusOS Monitor' and turn it on.",
                        buttonText = "Enable Accessibility",
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                }

                OnboardingState.Complete -> {
                    PermissionStep(
                        title = "System Ready",
                        description = "All permissions granted. Remember: The goal is not to use the phone less, but to use it with intention.",
                        buttonText = "Initialize FocusOS",
                        onClick = {
                            viewModel.onEvent(OnboardingEvent.FinishOnboarding)
                        }
                    )
                }

                OnboardingState.NavigatingToHome -> {
                    // Handle navigation to home screen
                }
            }

        }
    }
}

@Composable
private fun PermissionStep(
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.small
        ) {
            Text(text = buttonText, modifier = Modifier.padding(8.dp))
        }
    }
}