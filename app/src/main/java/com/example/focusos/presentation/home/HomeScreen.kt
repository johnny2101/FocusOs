package com.example.focusos.presentation.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.focusos.presentation.components.FrictionDialog

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val apps by viewModel.appList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val frictionPackage by viewModel.frictionState.collectAsState()

    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
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
                    items(apps) { app ->
                        AppItemView(
                            label = app.label,
                            onClick = {
                                viewModel.onAppClick(app.packageName)
                            }
                        )
                    }
                }

                frictionPackage?.let { packageName ->
                    FrictionDialog(
                        packageName = packageName,
                        onDismiss = viewModel::onFrictionDismissed,
                        onSuccess = viewModel::onFrictionPassed
                    )
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