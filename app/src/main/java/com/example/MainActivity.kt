package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.ui.FinanceViewModel
import com.example.ui.FinanceViewModelFactory
import com.example.ui.MainAppScreen
import com.example.ui.components.NotificationHelper
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.checkCardReminders(applicationContext)
        }
    }

    private val viewModel: FinanceViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(database.financeDao())
        FinanceViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create notification channel immediately
        NotificationHelper.createNotificationChannel(applicationContext)

        // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val useDark = (uiState.userSettings.isDarkMode == true) || (uiState.userSettings.isDarkMode == null && systemDark)

            MyApplicationTheme(darkTheme = useDark) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}
