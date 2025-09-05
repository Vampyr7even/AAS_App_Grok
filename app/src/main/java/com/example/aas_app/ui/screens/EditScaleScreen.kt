package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScaleScreen(navController: NavController, scaleId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    var scaleName by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(scaleId) {
        val scale = viewModel.getScaleById(scaleId)
        scale?.let {
            scaleName = it.scaleName
            options = it.options
        } ?: run {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading scale")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Scale",
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
            value = options,
            onValueChange = { options = it },
            label = { Text("Options") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (scaleName.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            viewModel.updateScale(
                                ScaleEntity(
                                    id = scaleId,
                                    scaleName = scaleName,
                                    options = options
                                )
                            )
                            snackbarHostState.showSnackbar("Scale updated successfully")
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("EditScaleScreen", "Error updating scale: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error updating scale: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Scale name cannot be blank")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Scale")
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}