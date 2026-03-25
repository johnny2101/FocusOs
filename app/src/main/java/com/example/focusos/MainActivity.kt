package com.example.focusos

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.focusos.presentation.MainViewModel
import com.example.focusos.presentation.home.HomeScreen
import com.example.focusos.presentation.onboarding.OnboardingScreen
import com.example.focusos.presentation.settings.SettingsScreen
import com.example.focusos.service.BackgroundMonitorService
import com.example.focusos.ui.theme.FocusOsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val navController = rememberNavController()

            // Start the background service immediately if onboarding is already complete
            if (viewModel.startDestination == "home") {
                startMonitorService()
            }

            FocusOsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = viewModel.startDestination
                    ) {
                        composable("onboarding") {
                            // Instantiate the ViewModel scoped to this navigation route
                            val onboardingViewModel: com.example.focusos.presentation.onboarding.OnboardingViewModel = hiltViewModel()

                            OnboardingScreen(
                                viewModel = onboardingViewModel,
                                onOnboardingFinished = {
                                    // Start service once permissions are granted
                                    startMonitorService()
                                    // Navigate to home and prevent returning to onboarding
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                onOpenSettings = { navController.navigate("settings") }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun startMonitorService() {
        val intent = Intent(this, BackgroundMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}