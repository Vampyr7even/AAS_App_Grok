package com.example.aas_app.ui.screens.pecl

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun TasksTab(navController: NavController, poiId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val tasksState by viewModel.tasksState.observeAsState(AppState.Success(emptyList()))
    val poisSimple by viewModel.poisSimple.observeAsState(emptyList())
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskName by remember { mutableStateOf("") }
    var selectedPoisForAdd by remember { mutableStateOf(setOf<Long>()) }
    var showEditTaskDialog by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var editTaskName by remember { mutableStateOf("") }
    var selectedPoisForEdit by remember { mutableStateOf(setOf<Long>()) }
    var selectedTaskToDelete by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var showTaskDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Log.d("TasksTab", "Loading tasks and POIs")
        try {
            viewModel.loadAllPois()
            viewModel.loadTasksForPoi(poiId)
        } catch (e: Exception) {
            Log.e("TasksTab", "Error loading tasks: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading tasks: ${e.message}")
            }
        }
    }

    LaunchedEffect(showEditTaskDialog) {
        showEditTaskDialog?.let { task ->
            editTaskName = task.name
            coroutineScope.launch {
                try {
                    val poiIds = viewModel.getPoisForTask(task.id).first().map { poi -> poi.id }.toSet()
                    selectedPoisForEdit = poiIds
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error loading POIs for task: ${e.message}")
                    }
                }
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
            text = "POI Tasks",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { showAddTaskDialog = true }) {
            Icon(Icons.Filled.Add, contentDescription = "Add Task")
        }
        Text("Add Task", modifier = Modifier.padding(start = 4.dp))
    }

    when (val state = tasksState) {
        is AppState.Loading -> Text(text = "Loading...")
        is AppState.Success -> {
            if (state.data.isEmpty()) {
                Text("No Tasks have been entered in the database. Add Tasks to begin.")
            } else {
                LazyColumn {
                    items(state.data) { task ->
                        val poisForTask = remember { mutableStateOf<List<PeclPoiEntity>>(emptyList()) }
                        LaunchedEffect(task.id) {
                            try {
                                poisForTask.value = viewModel.getPoisForTask(task.id).first()
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error loading POIs for task: ${e.message}")
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = task.name)
                                Text(
                                    text = "POI: ${poisForTask.value.joinToString(", ") { it.name }}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { showEditTaskDialog = task }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                selectedTaskToDelete = task
                                showTaskDeleteDialog = true
                            }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
        is AppState.Error -> Text("Error: ${state.message}")
    }

    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Task") },
            text = {
                Column {
                    TextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text("Task Name") }
                    )
                    Text(text = "Select POIs:")
                    LazyColumn {
                        items(poisSimple) { poi ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedPoisForAdd.contains(poi.id),
                                    onCheckedChange = { checked ->
                                        selectedPoisForAdd = if (checked) {
                                            selectedPoisForAdd + poi.id
                                        } else {
                                            selectedPoisForAdd - poi.id
                                        }
                                    }
                                )
                                Text(text = poi.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskName.isNotBlank() && selectedPoisForAdd.isNotEmpty()) {
                            coroutineScope.launch {
                                try {
                                    viewModel.insertTask(PeclTaskEntity(name = newTaskName), selectedPoisForAdd.toList())
                                    showAddTaskDialog = false
                                    newTaskName = ""
                                    selectedPoisForAdd = emptySet()
                                    Toast.makeText(context, "POI Task added successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("TasksTab", "Error adding task: ${e.message}", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error adding task: ${e.message}")
                                    }
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Task name and at least one POI are required")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = newTaskName.isNotBlank() && selectedPoisForAdd.isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAddTaskDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    showEditTaskDialog?.let { task ->
        AlertDialog(
            onDismissRequest = { showEditTaskDialog = null },
            title = { Text("Edit Task") },
            text = {
                Column {
                    TextField(
                        value = editTaskName,
                        onValueChange = { editTaskName = it },
                        label = { Text("Task Name") }
                    )
                    Text(text = "Select POIs:")
                    LazyColumn {
                        items(poisSimple) { poi ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedPoisForEdit.contains(poi.id),
                                    onCheckedChange = { checked ->
                                        selectedPoisForEdit = if (checked) {
                                            selectedPoisForEdit + poi.id
                                        } else {
                                            selectedPoisForEdit - poi.id
                                        }
                                    }
                                )
                                Text(text = poi.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTaskName.isNotBlank() && selectedPoisForEdit.isNotEmpty()) {
                            coroutineScope.launch {
                                try {
                                    viewModel.updateTask(task.copy(name = editTaskName), selectedPoisForEdit.toList())
                                    showEditTaskDialog = null
                                    editTaskName = ""
                                    selectedPoisForEdit = emptySet()
                                    Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("TasksTab", "Error updating task: ${e.message}", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error updating task: ${e.message}")
                                    }
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Task name and at least one POI are required")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = editTaskName.isNotBlank() && selectedPoisForEdit.isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditTaskDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showTaskDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showTaskDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this task?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                selectedTaskToDelete?.let { viewModel.deleteTask(it) }
                                showTaskDeleteDialog = false
                                Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("TasksTab", "Error deleting task: ${e.message}", e)
                                snackbarHostState.showSnackbar("Error deleting task: ${e.message}")
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
                    onClick = { showTaskDeleteDialog = false },
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