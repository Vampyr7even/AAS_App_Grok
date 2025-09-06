package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScaleScreen(
    navController: NavController,
    scaleId: Long,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    var scaleName by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val scale = viewModel.getScaleById(scaleId)
            scale?.let {
                scaleName = it.scaleName
                options = it.options
            }
        } catch (e: Exception) {
            Log.e("EditScaleScreen", "Error loading scale: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading scale: ${e.message}")
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
            label = { Text("Scale Options (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (scaleName.isNotBlank() && options.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            viewModel.updateScale(ScaleEntity(id = scaleId, scaleName = scaleName, options = options))
                            Toast.makeText(context, "Scale updated successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("EditScaleScreen", "Error updating scale: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error updating scale: ${e.message}")
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
            Text("Save")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}