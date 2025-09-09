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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScaleScreen(
    navController: NavController,
    scaleId: Long,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val scaleState by viewModel.scaleState.observeAsState(State.Success(null))
    var scaleName by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(scaleId) {
        viewModel.getScaleById(scaleId)
    }

    LaunchedEffect(scaleState) {
        when (val state = scaleState) {
            is State.Success -> {
                state.data?.let { scale ->
                    scaleName = scale.scaleName
                    options = scale.options
                }
            }
            is State.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading scale: ${state.message}")
                }
            }
            is State.Loading -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Scale",
            style = MaterialTheme.typography.headlineSmall
        )

        when (val state = scaleState) {
            is State.Loading -> CircularProgressIndicator()
            is State.Success -> {
                TextField(
                    value = scaleName,
                    onValueChange = { scaleName = it },
                    label = { Text("Scale Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                TextField(
                    value = options,
                    onValueChange = { options = it },
                    label = { Text("Options (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (scaleName.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        val updatedScale = ScaleEntity(
                                            id = scaleId,
                                            scaleName = scaleName,
                                            options = options
                                        )
                                        viewModel.updateScale(updatedScale)
                                        Toast.makeText(context, "Scale updated successfully", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    } catch (e: Exception) {
                                        Log.e("EditScaleScreen", "Error updating scale: ${e.message}", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Error updating scale: ${e.message}")
                                        }
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Scale name cannot be blank")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
            is State.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}