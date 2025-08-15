package com.example.aas_app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationTopBar(navController: NavHostController) {
    val currentBackStackEntryState = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntryState.value?.destination?.route
    TopAppBar(
        title = { Text("APEX Analytics Suite") },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                NavigationButton("Admin", currentRoute?.startsWith("admin") == true) { navController.navigate("admin/programs") }
                NavigationButton("Evaluate", currentRoute == "evaluate") { navController.navigate("evaluate") }
                NavigationButton("Builder", currentRoute == "builder") { navController.navigate("builder") }
                NavigationButton("Repository", currentRoute == "repository") { navController.navigate("repository") }
                NavigationButton("Survey", currentRoute == "survey") { navController.navigate("survey") }
                // Add more as needed
            }
        }
    )
}

@Composable
fun NavigationButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFE57373) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Black
        )
    ) {
        Text(text)
    }
}