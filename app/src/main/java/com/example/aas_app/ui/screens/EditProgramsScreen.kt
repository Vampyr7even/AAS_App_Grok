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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun EditProgramsScreen(
    navController: NavController,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val programsState by viewModel.programsState.observeAsState(State.Success(emptyList()))
    var showAddProgramDialog by remember { mutableStateOf(false) }
    var newProgramName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<PeclProgramEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadPrograms()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Programs",
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
                text = "Programs",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddProgramDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Program")
            }
            Text("Add Program", modifier = Modifier.padding(start = 4.dp))
        }

        when (val state = programsState) {
            is State.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is State.Success -> {
                if (state.data.isEmpty()) {
                    Text("No programs have been entered in the database. Add Programs to begin.")
                } else {
                    LazyColumn {
                        items(state.data) { program ->
                            var isEditing by remember { mutableStateOf(false) }
                            var editName by remember { mutableStateOf(program.name) }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isEditing) {
                                    TextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        label = { Text("Program Name") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        if (editName.isNotBlank()) {
                                            coroutineScope.launch {
                                                try {
                                                    viewModel.updateProgram(program.copy(name = editName))
                                                    Toast.makeText(context, "Program updated successfully", Toast.LENGTH_SHORT).show()
                                                    isEditing = false
                                                } catch (e: Exception) {
                                                    Log.e("EditProgramsScreen", "Error updating program: ${e.message}", e)
                                                    snackbarHostState.showSnackbar("Error updating program: ${e.message}")
                                                }
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Program name cannot be blank")
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Filled.Check, contentDescription = "Save")
                                    }
                                    IconButton(onClick = { isEditing = false }) {
                                        Icon(Icons.Filled.Close, contentDescription = "Cancel")
                                    }
                                } else {
                                    Text(text = program.name, modifier = Modifier.weight(1f))
                                    IconButton(onClick = { isEditing = true }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit Program")
                                    }
                                    IconButton(onClick = { showDeleteDialog = program }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Program")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is State.Error -> Text(
                text = "Error: ${state.message}",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showAddProgramDialog) {
            AlertDialog(
                onDismissRequest = { showAddProgramDialog = false },
                title = { Text("Add Program") },
                text = {
                    TextField(
                        value = newProgramName,
                        onValueChange = { newProgramName = it },
                        label = { Text("Program Name") }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newProgramName.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        viewModel.insertProgram(PeclProgramEntity(name = newProgramName))
                                        showAddProgramDialog = false
                                        newProgramName = ""
                                        Toast.makeText(context, "Program added successfully", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("EditProgramsScreen", "Error adding program: ${e.message}", e)
                                        snackbarHostState.showSnackbar("Error adding program: ${e.message}")
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Program name cannot be blank")
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
                        onClick = { showAddProgramDialog = false },
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
                text = { Text("Delete this program?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    viewModel.deleteProgram(showDeleteDialog!!)
                                    snackbarHostState.showSnackbar("Program deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("EditProgramsScreen", "Error deleting program: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting program: ${e.message}")
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