package com.example.aas_app.ui.screens.pecl

import android.util.Log
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.launch

@Composable
fun TasksTab(navController: NavController, poiId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading)
    var showDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var newTaskName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(poiId) {
        viewModel.loadTasksForPoi(poiId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = tasksState) {
            is AppState.Loading -> Text("Loading...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { task: PeclTaskEntity ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = task.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = { navController.navigate("editTask/${task.id}/$poiId") }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedTask = task; showDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        TextField(
            value = newTaskName,
            onValueChange = { newTaskName = it },
            label = { Text("New Task Name") }
        )
        Button(
            onClick = {
                if (newTaskName.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            viewModel.insertTask(PeclTaskEntity(name = newTaskName), listOf(poiId))
                            newTaskName = ""
                            snackbarHostState.showSnackbar("Task added successfully")
                        } catch (e: Exception) {
                            Log.e("TasksTab", "Error adding task: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error adding task: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Task name cannot be blank")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add Task")
        }
        SnackbarHost(hostState = snackbarHostState)

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Delete") },
                text = { Text("Delete this task?") },
                confirmButton = {
                    Button(onClick = {
                        selectedTask?.let { task ->
                            coroutineScope.launch {
                                try {
                                    viewModel.deleteTask(task)
                                    snackbarHostState.showSnackbar("Task deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("TasksTab", "Error deleting task: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting task: ${e.message}")
                                }
                            }
                        }
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
}