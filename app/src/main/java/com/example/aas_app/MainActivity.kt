package com.example.aas_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aas_app.ui.screens.PeclScreen
import com.example.aas_app.ui.theme.AAS_AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContent {
                AAS_AppTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error setting content: ${e.message}", e)
            // Optional: Handle error, e.g., show fallback UI or toast
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // TODO: Replace with dynamic instructorId from authentication or intent extras (e.g., from login)
    val instructorId = 1L // Default for testing

    NavHost(navController = navController, startDestination = "peclScreen") {
        composable("peclScreen") {
            PeclScreen(navController = navController, instructorId = instructorId)
        }
    }
}