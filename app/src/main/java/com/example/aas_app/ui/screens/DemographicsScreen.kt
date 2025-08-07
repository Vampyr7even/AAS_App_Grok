package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun DemographicsScreen(navController: NavController) {
    var selectedNav by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Demographics",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            listOf(
                "Survey" to "survey",
                "Builder" to "builder",
                "Repository" to "repository"
            ).forEach { (label, route) ->
                Button(
                    onClick = {
                        selectedNav = route
                        try {
                            navController.navigate(route)
                        } catch (e: Exception) {
                            Log.e("DemographicsScreen", "Navigation failed to $route", e)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedNav == route) Color(0xFFE57373) else Color.Transparent,
                        contentColor = Color.Black
                    ),
                    shape = RectangleShape,
                    modifier = Modifier
                ) {
                    Text(label)
                }
            }
        }
    }
}