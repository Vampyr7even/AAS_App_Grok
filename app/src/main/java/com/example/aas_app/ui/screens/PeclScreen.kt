package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import android.util.Log

@Composable
fun PeclScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            try {
                navController.navigate("evaluate/default_program/default_poi")
            } catch (e: Exception) {
                Log.e("PeclScreen", "Navigation failed", e)
            }
        }) { Text("Evaluate") }
        Button(onClick = { navController.navigate("edit_pois") }) { Text("Edit POIs") }
        Button(onClick = { navController.navigate("edit_programs") }) { Text("Edit Programs") }
        Button(onClick = { navController.navigate("edit_tasks") }) { Text("Edit Tasks") }
        Button(onClick = { navController.navigate("edit_questions") }) { Text("Edit Questions") }
        Button(onClick = { navController.navigate("edit_scales") }) { Text("Edit Scales") }
    }
}