package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController

@Composable
fun ExaminationsScreen(navController: NavController, moduleName: String) {
    Text(
        text = "$moduleName - Under Development",
        modifier = Modifier.fillMaxSize(),
        textAlign = TextAlign.Center
    )
}