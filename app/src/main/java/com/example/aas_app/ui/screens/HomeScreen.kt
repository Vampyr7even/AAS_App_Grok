package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    var selectedNav by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var navigationFailed by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "APEX Analytics Suite",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (navigationFailed) {
            Text(
                text = "Navigation to PECL failed. Check logs for details.",
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            listOf(
                "Demographics" to "demographics",
                "Surveys" to "survey",
                "PECL" to "peclScreen",
                "Examinations" to "examinations",
                "Analytics" to "analytics",
                "Administration" to "admin/programs"
            ).forEach { (label, route) ->
                Button(
                    onClick = {
                        selectedNav = route
                        try {
                            Log.d("HomeScreen", "Navigating to $route from current route: ${navController.currentBackStackEntry?.destination?.route}")
                            navController.navigate(route)
                        } catch (e: Exception) {
                            Log.e("HomeScreen", "Navigation error to $route: ${e.message}", e)
                            navigationFailed = route == "peclScreen"
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedNav == route) Color(0xFFE57373) else Color.Transparent,
                        contentColor = if (selectedNav == route) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Text(label)
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}