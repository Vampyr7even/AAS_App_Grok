package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entities.PeclScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScaleScreen(navController: NavController, scaleId: Long) {
    val viewModel: AdminViewModel = hiltViewModel()
    val scaleState by viewModel.scalesState.observeAsState(AppState.Loading<List<PeclScaleEntity>>())

    LaunchedEffect(scaleId) {
        viewModel.loadScales()
    }

    var scaleName by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("") }

    when (val state = scaleState) {
        is AppState.Success -> {
            val scale = state.data.firstOrNull { it.id == scaleId } ?: return
            scaleName = scale.scale
            options = scale.options
        }
        else -> { }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = scaleName,
            onValueChange = { scaleName = it },
            label = { Text("Scale Name") }
        )
        TextField(
            value = options,
            onValueChange = { options = it },
            label = { Text("Options") }
        )
        Button(
            onClick = {
                val updatedScale = PeclScaleEntity(scaleId, scaleName, options)
                viewModel.updateScale(updatedScale) // Assume added to ViewModel/Repo
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Update")
        }
    }
}