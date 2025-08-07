package com.example.aas_app.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.aas_app.viewmodel.DemographicsViewModel

@Composable
fun DemographicsScreen(demographicsViewModel: DemographicsViewModel, navController: NavController) {
    Text("Demographics Screen")

    Button(onClick = { navController.navigate("updateUsers") }) {
        Text("Update Users")
    }

    Button(onClick = { navController.navigate("updateProjects") }) {
        Text("Update Projects")
    }
}