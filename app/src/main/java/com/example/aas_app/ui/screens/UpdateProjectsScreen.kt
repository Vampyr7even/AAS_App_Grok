package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProjectsScreen(navController: NavController) {
    val viewModel: AdminViewModel = hiltViewModel()
    val programsState by viewModel.programsState.observeAsState(AppState.Loading as AppState<List<PeclProgramEntity>>)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDialog by remember { mutableStateOf(false) }
    var selectedProgram by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var newProgramName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadPrograms()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = programsState) {
            is AppState.Loading -> Text("Loading...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { program ->
                        Row {
                            Text(program.name)
                            IconButton(onClick = { /* Edit logic */ }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Program")
                            }
                            IconButton(onClick = { selectedProgram = program; showDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete Program")
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        TextField(
            value = newProgramName,
            onValueChange = { newProgramName = it },
            label = { Text("New Program Name") }
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        viewModel.insertProgram(PeclProgramEntity(0L, newProgramName))
                        newProgramName = ""
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Error adding program: ${e.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add Program")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Delete") },
                text = { Text("Delete this program?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    selectedProgram?.let { viewModel.deleteProgram(it) }
                                    showDialog = false
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error deleting program: ${e.message}")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDialog = false },
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