package com.example.aas_app.ui.screens

import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTasksScreen(navController: NavController, poiId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading)
    var selectedTask by remember { mutableStateOf<PeclTaskEntity?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
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

        when (val state = tasksState) {
            is AppState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { task: PeclTaskEntity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = task.name)
                            Row {
                                IconButton(onClick = { navController.navigate("editTask/${task.id}/$poiId") }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Task")
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            viewModel.deleteTask(task)
                                            snackbarHostState.showSnackbar("Task deleted successfully")
                                        } catch (e: Exception) {
                                            Log.e("EditTasksScreen", "Error deleting task: ${e.message}", e)
                                            snackbarHostState.showSnackbar("Error deleting task: ${e.message}")
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Task")
                                }
                            }
                        }
                    }
                }
            }
            is AppState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("addTask/$poiId") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Task")
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}