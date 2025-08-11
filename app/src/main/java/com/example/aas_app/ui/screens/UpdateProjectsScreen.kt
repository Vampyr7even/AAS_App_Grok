package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProjectsScreen(navController: NavController) {
    val viewModel: AdminViewModel = hiltViewModel()
    val programsState by viewModel.programsState.observeAsState(AppState.Loading<List<PeclProgramEntity>>())

    LaunchedEffect(Unit) {
        viewModel.loadPrograms()
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedProgram by remember { mutableStateOf<PeclProgramEntity?>(null) }

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
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedProgram = program; showDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        var newProgramName by remember { mutableStateOf("") }
        TextField(
            value = newProgramName,
            onValueChange = { newProgramName = it },
            label = { Text("New Program Name") }
        )
        Button(
            onClick = { viewModel.insertProgram(PeclProgramEntity(0, newProgramName)) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add Program")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this program?") },
            confirmButton = {
                Button(onClick = {
                    selectedProgram?.let { viewModel.deleteProgram(it) }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}