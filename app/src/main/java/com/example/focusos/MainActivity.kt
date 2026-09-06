package com.example.focusos

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.focusos.presentation.MainViewModel
import com.example.focusos.presentation.analytics.AnalyticScreen
import com.example.focusos.presentation.home.HomeScreen
import com.example.focusos.presentation.onboarding.OnboardingScreen
import com.example.focusos.presentation.settings.SettingsScreen
import com.example.focusos.service.BackgroundMonitorService
import com.example.focusos.service.WeeklyReportWorker
import com.example.focusos.ui.theme.FocusOsTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val navController = rememberNavController()

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
                                    scheduleWeeklyReport()
                                    startMonitorService()
                                    // Navigate to home and prevent returning to onboarding
                                    navController.navigate("home") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {

                            LaunchedEffect(Unit) {
                                startMonitorService()
                            }

                            HomeScreen(
                                onOpenSettings = { navController.navigate("settings") },
                                onOpenAnalytics = { navController.navigate("analytics") }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("analytics") {
                            AnalyticScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun scheduleWeeklyReport() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WeeklyReportWorker>(7, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WeeklyGitReportWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun startMonitorService() {
        val serviceIntent = Intent(this, BackgroundMonitorService::class.java)
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}