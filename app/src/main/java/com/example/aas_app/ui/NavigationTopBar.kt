package com.example.aas_app.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationTopBar(navController: NavHostController) {
    val currentBackStackEntryState = navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntryState.value?.destination?.route
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    TopAppBar(
        title = { Text("APEX Analytics Suite") },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        actions = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                NavigationButton("Admin", currentRoute?.startsWith("admin") == true) {
                    try {
                        Log.d("NavigationTopBar", "Navigating to admin/programs from $currentRoute")
                        navController.navigate("admin/programs")
                    } catch (e: Exception) {
                        Log.e("NavigationTopBar", "Navigation error to admin/programs: ${e.message}", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                        }
                    }
                }
                NavigationButton("PECL", currentRoute == "peclScreen") {
                    try {
                        Log.d("NavigationTopBar", "Navigating to peclScreen from $currentRoute")
                        navController.navigate("peclScreen")
                    } catch (e: Exception) {
                        Log.e("NavigationTopBar", "Navigation error to peclScreen: ${e.message}", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                        }
                    }
                }
                NavigationButton("Builder", currentRoute == "builder") {
                    try {
                        Log.d("NavigationTopBar", "Navigating to builder from $currentRoute")
                        navController.navigate("builder")
                    } catch (e: Exception) {
                        Log.e("NavigationTopBar", "Navigation error to builder: ${e.message}", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                        }
                    }
                }
                NavigationButton("Repository", currentRoute == "repository") {
                    try {
                        Log.d("NavigationTopBar", "Navigating to repository from $currentRoute")
                        navController.navigate("repository")
                    } catch (e: Exception) {
                        Log.e("NavigationTopBar", "Navigation error to repository: ${e.message}", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                        }
                    }
                }
                NavigationButton("Survey", currentRoute == "survey") {
                    try {
                        Log.d("NavigationTopBar", "Navigating to survey from $currentRoute")
                        navController.navigate("survey")
                    } catch (e: Exception) {
                        Log.e("NavigationTopBar", "Navigation error to survey: ${e.message}", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                        }
                    }
                }
            }
            SnackbarHost(hostState = snackbarHostState)
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