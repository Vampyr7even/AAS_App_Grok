package com.example.aas_app.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.aas_app.viewmodel.PeclViewModel

@Composable
fun PeclScreen(peclViewModel: PeclViewModel, navController: NavController) {
    Text("PECL Screen")

    Button(onClick = { navController.navigate("evaluate/1") }) {  // Placeholder poiId
        Text("Evaluate")
    }
}