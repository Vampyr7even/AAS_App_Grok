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
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun EditTasksScreen(
    navController: NavController,
    poiId: Long,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val tasksState by viewModel.tasksState.observeAsState(AppState.Success(emptyList()))
    val poisSimple by viewModel.poisSimple.observeAsState(emptyList())
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskName by remember { mutableStateOf("") }
    var selectedPoisForAdd by remember { mutableStateOf(setOf<Long>()) }
    var showEditTaskDialog by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var editTaskName by remember { mutableStateOf("") }
    var selectedPoisForEdit by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteDialog by remember { mutableStateOf<PeclTaskEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadTasksForPoi(poiId)
        viewModel.loadAllPois()
    }

    LaunchedEffect(showEditTaskDialog) {
        showEditTaskDialog?.let { task ->
            editTaskName = task.name
            coroutineScope.launch {
                try {
                    val poisForTask = viewModel.getPoisForTask(task.id).first()
                    selectedPoisForEdit = poisForTask.map { it.id }.toSet<Long>()
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error loading POIs for task: ${e.message}")
                    }
                }
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
            text = "Edit Tasks",
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
            is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = task.name)
                                    Text(
                                        text = "POI: ${poisForTask.value.joinToString(", ") { it.name }}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { showEditTaskDialog = task }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit Task")
                                }
                                IconButton(onClick = { showDeleteDialog = task }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Task")
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
                                        Log.e("EditTasksScreen", "Error adding task: ${e.message}", e)
                                        snackbarHostState.showSnackbar("Error adding task: ${e.message}")
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
                                        Log.e("EditTasksScreen", "Error updating task: ${e.message}", e)
                                        snackbarHostState.showSnackbar("Error updating task: ${e.message}")
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

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Confirm Delete") },
                text = { Text("Delete this task?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    viewModel.deleteTask(showDeleteDialog!!)
                                    snackbarHostState.showSnackbar("Task deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("EditTasksScreen", "Error deleting task: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting task: ${e.message}")
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