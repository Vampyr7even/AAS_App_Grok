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
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.TaskWithPois

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTasksScreen(navController: NavController, poiId: Long) {
    val viewModel: AdminViewModel = hiltViewModel()
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading as AppState<List<TaskWithPois>>)

    LaunchedEffect(poiId) {
        viewModel.loadTasksForPoi(poiId)
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<TaskWithPois?>(null) }
    var editTask by remember { mutableStateOf<TaskWithPois?>(null) }
    var editName by remember { mutableStateOf("") }
    var newTaskName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = tasksState) {
            is AppState.Loading -> Text(text = "Loading...")
            is AppState.Success -> {
                LazyColumn {
                    items(items = state.data) { taskWithPois ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = taskWithPois.task.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                editTask = taskWithPois
                                editName = taskWithPois.task.name
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                selectedTask = taskWithPois
                                showDialog = true
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text(text = "Error: ${state.message}")
        }

        TextField(
            value = newTaskName,
            onValueChange = { newTaskName = it },
            label = { Text("New Task Name") }
        )
        Button(
            onClick = {
                viewModel.insertTask(PeclTaskEntity(name = newTaskName), listOf(poiId))
                newTaskName = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add Task")
        }

        editTask?.let { taskWithPois ->
            AlertDialog(
                onDismissRequest = { editTask = null },
                title = { Text("Edit Task") },
                text = {
                    TextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Task Name") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateTask(taskWithPois.task.copy(name = editName), null)
                        editTask = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { editTask = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this task?") },
            confirmButton = {
                Button(onClick = {
                    selectedTask?.let { viewModel.deleteTask(it.task) }
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