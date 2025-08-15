package com.example.aas_app.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.viewmodel.DemographicsViewModel

@Composable
fun DemographicsScreen(navController: NavController) {
    val demographicsViewModel = hiltViewModel<DemographicsViewModel>()

    Text("Demographics Screen")

    Button(onClick = { navController.navigate("update_users") }) {
        Text("Update Users")
    }

    Button(onClick = { navController.navigate("update_projects") }) {
        Text("Update Projects")
    }
}