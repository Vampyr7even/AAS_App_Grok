package com.example.aas_app.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    var selectedNav by remember { mutableStateOf<String?>(null) }

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
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            listOf(
                "Demographics" to "demographics",
                "Surveys" to "surveys",
                "PECL" to "pecl",
                "Examinations" to "examinations",
                "Analytics" to "analytics",
                "Administration" to "administration"
            ).forEach { (label, route) ->
                Button(
                    onClick = {
                        selectedNav = route
                        navController.navigate(route)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedNav == route) Color(0xFFE57373) else Color.Transparent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.border(1.dp, Color.Gray, RoundedCornerShape(0.dp))
                ) {
                    Text(label)
                }
            }
        }
    }
}