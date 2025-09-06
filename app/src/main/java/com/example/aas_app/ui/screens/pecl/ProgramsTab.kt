package com.example.aas_app.ui.screens.pecl

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ProgramsTab(
    adminViewModel: AdminViewModel,
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    val programsState by adminViewModel.programsState.observeAsState(AppState.Success(emptyList<PeclProgramEntity>()))
    var showAddProgram by remember { mutableStateOf(false) }
    var newProgramName by remember { mutableStateOf("") }
    var selectedProgramToDelete by remember { mutableStateOf<PeclProgramEntity?>(null) }

    LaunchedEffect(Unit) {
        Log.d("ProgramsTab", "Loading programs")
        try {
            adminViewModel.loadPrograms()
        } catch (e: Exception) {
            Log.e("ProgramsTab", "Error loading programs: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading programs: ${e.message}")
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Programs",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { showAddProgram = !showAddProgram }) {
            Icon(Icons.Filled.Add, contentDescription = "Add Program")
        }
        Text("Add Program", modifier = Modifier.padding(start = 4.dp))
    }

    when (val state = programsState) {
        is AppState.Loading -> Text("Loading...")
        is AppState.Success -> {
            LazyColumn {
                items(state.data) { program ->
                    var isEditing by remember { mutableStateOf(false) }
                    var editName by remember { mutableStateOf(program.name) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEditing) {
                            TextField(
                                value = editName,
                                onValueChange = { editName = it },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                try {
                                    adminViewModel.updateProgram(program.copy(name = editName))
                                    isEditing = false
                                    Toast.makeText(context, "Program updated", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("ProgramsTab", "Error updating program: ${e.message}", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error updating program: ${e.message}")
                                    }
                                }
                            }) {
                                Icon(imageVector = Icons.Filled.Save, contentDescription = "Save")
                            }
                            IconButton(onClick = {
                                editName = program.name
                                isEditing = false
                            }) {
                                Icon(imageVector = Icons.Filled.Cancel, contentDescription = "Cancel")
                            }
                        } else {
                            Text(text = program.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                isEditing = true
                                editName = program.name
                            }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Program")
                            }
                            IconButton(onClick = { selectedProgramToDelete = program }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete Program")
                            }
                        }
                    }
                }
            }
        }
        is AppState.Error -> Text("Error: ${state.message}")
    }

    if (showAddProgram) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = newProgramName,
                onValueChange = { newProgramName = it },
                label = { Text("New Program Name") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                if (newProgramName.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            adminViewModel.insertProgram(PeclProgramEntity(0L, newProgramName))
                            newProgramName = ""
                            showAddProgram = false
                            Toast.makeText(context, "Program added successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("ProgramsTab", "Error adding program: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error adding program: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Program name cannot be blank")
                    }
                }
            }) {
                Icon(imageVector = Icons.Filled.Save, contentDescription = "Save")
            }
        }
    }

    selectedProgramToDelete?.let { program ->
        AlertDialog(
            onDismissRequest = { selectedProgramToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this program?") },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            adminViewModel.deleteProgram(program)
                            selectedProgramToDelete = null
                            Toast.makeText(context, "Program deleted successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("ProgramsTab", "Error deleting program: ${e.message}", e)
                            coroutineScope.launch {
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
                    onClick = { selectedProgramToDelete = null },
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