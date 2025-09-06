package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScalesScreen(
    navController: NavController,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val scalesState by viewModel.scalesState.observeAsState(AppState.Success(emptyList()))
    var showAddScaleDialog by remember { mutableStateOf(false) }
    var newScaleName by remember { mutableStateOf("") }
    var newScaleOptions by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<ScaleEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadScales()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Scales",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Scales",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddScaleDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Scale")
            }
            Text("Add Scale", modifier = Modifier.padding(start = 4.dp))
        }

        when (val state = scalesState) {
            is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is AppState.Success -> {
                if (state.data.isEmpty()) {
                    Text("No scales have been entered in the database. Add Scales to begin.")
                } else {
                    LazyColumn {
                        items(state.data) { scale ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = scale.scaleName)
                                    Text(
                                        text = "Options: ${scale.options}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = {
                                    navController.navigate("editScale/${scale.id}")
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit Scale")
                                }
                                IconButton(onClick = { showDeleteDialog = scale }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Scale")
                                }
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text(
                text = "Error: ${state.message}",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showAddScaleDialog) {
            AlertDialog(
                onDismissRequest = { showAddScaleDialog = false },
                title = { Text("Add Scale") },
                text = {
                    Column {
                        TextField(
                            value = newScaleName,
                            onValueChange = { newScaleName = it },
                            label = { Text("Scale Name") }
                        )
                        TextField(
                            value = newScaleOptions,
                            onValueChange = { newScaleOptions = it },
                            label = { Text("Scale Options (comma-separated)") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newScaleName.isNotBlank() && newScaleOptions.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        viewModel.insertScale(ScaleEntity(scaleName = newScaleName, options = newScaleOptions))
                                        showAddScaleDialog = false
                                        newScaleName = ""
                                        newScaleOptions = ""
                                        Toast.makeText(context, "Scale added successfully", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("EditScalesScreen", "Error adding scale: ${e.message}", e)
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
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showAddScaleDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Confirm Delete") },
                text = { Text("Delete this scale?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    viewModel.deleteScale(showDeleteDialog!!)
                                    snackbarHostState.showSnackbar("Scale deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("EditScalesScreen", "Error deleting scale: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting scale: ${e.message}")
                                }
                            }
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("No")
                    }
                }
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}