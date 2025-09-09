package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScaleScreen(
    navController: NavController,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val scalesState by viewModel.scalesState.observeAsState(State.Success(emptyList()))
    var scaleName by remember { mutableStateOf("") }
    var scaleOptions by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Add Scale",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = scaleName,
            onValueChange = { scaleName = it },
            label = { Text("Scale Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = scaleOptions,
            onValueChange = { scaleOptions = it },
            label = { Text("Scale Options (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (scaleName.isNotBlank() && scaleOptions.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            val scale = ScaleEntity(scaleName = scaleName, options = scaleOptions)
                            viewModel.insertScale(scale)
                            Toast.makeText(context, "Scale added successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("AddScaleScreen", "Error adding scale: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error adding scale: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Scale name and options are required")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Scale")
        }

        when (val state = scalesState) {
            is State.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is State.Success -> {
                // Optionally display scales if needed
            }
            is State.Error -> Text(
                text = "Error: ${state.message}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}