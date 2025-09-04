package com.example.aas_app.ui.screens.pecl

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun EvaluateTab(navController: NavController, errorMessage: String?, snackbarHostState: SnackbarHostState, coroutineScope: CoroutineScope) {
    Log.d("EvaluateTab", "Rendering EvaluateTab")
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Evaluate - Under Development")
        Button(
            onClick = {
                try {
                    Log.d("EvaluateTab", "Navigating to pecl/evaluate")
                    navController.navigate("pecl/evaluate")
                } catch (e: Exception) {
                    Log.e("EvaluateTab", "Navigation error to pecl/evaluate: ${e.message}", e)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Go to Evaluate")
        }
    }
}