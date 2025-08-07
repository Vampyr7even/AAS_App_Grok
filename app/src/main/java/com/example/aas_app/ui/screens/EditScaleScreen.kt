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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aas_app.data.Result
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.PeclViewModel

@Composable
fun EditScaleScreen(navController: NavController, viewModel: PeclViewModel, scaleId: Int) {
    val context = LocalContext.current
    var scale by remember { mutableStateOf<ScaleEntity?>(null) }
    var scaleName by remember { mutableStateOf("") }
    var scaleData by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(scaleId) {
        val result = viewModel.getScaleById(scaleId)
        if (result is Result.Success) {
            scale = result.data
            scaleName = result.data?.scaleName ?: ""
            scaleData = result.data?.scaleData ?: ""
        }
        isLoading = false
    }

    if (isLoading) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    if (scale == null) {
        Text("Scale not found", modifier = Modifier.padding(16.dp))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Edit Scale",
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
                val updatedScale = scale!!.copy(
                    scaleName = scaleName.trim(),
                    scaleData = scaleData.trim()
                )
                viewModel.updateScale(updatedScale)
                Toast.makeText(context, "Scale updated", Toast.LENGTH_SHORT).show()
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