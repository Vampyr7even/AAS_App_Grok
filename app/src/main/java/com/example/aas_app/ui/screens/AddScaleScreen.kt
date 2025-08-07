package com.example.aas_app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.PeclViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AddScaleScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: PeclViewModel = koinViewModel()
    var scaleName by remember { mutableStateOf("") }
    var scaleData by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add Scale",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        TextField(
            value = scaleName,
            onValueChange = { scaleName = it },
            label = { Text("Scale Name") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = scaleData,
            onValueChange = { scaleData = it },
            label = { Text("Scale Data") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Button(
            onClick = {
                if (scaleName.isBlank() || scaleData.isBlank()) {
                    Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val newScale = ScaleEntity(
                    scaleName = scaleName.trim(),
                    scaleData = scaleData.trim()
                )
                viewModel.insertScale(newScale)
                Toast.makeText(context, "Scale added", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE57373),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Save")
        }
    }
}