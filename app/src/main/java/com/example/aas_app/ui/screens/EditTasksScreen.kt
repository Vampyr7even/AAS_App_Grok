package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
modifier = Modifier.fillMaxSize().padding(16.dp),
verticalArrangement = Arrangement.Center,
horizontalAlignment = Alignment.CenterHorizontally
) {
    when (val state = tasksState) {
        is AppState.Loading -> Text("Loading...")
        is AppState.Success -> {
            LazyColumn {
                items(state.data) { task ->
                    Row {
                        Text(task.name)
                        IconButton(onClick = { /* Edit logic */ }) {
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

    var newTaskName by remember { mutableStateOf("") }
    TextField(
        value = newTaskName,
        onValueChange = { newTaskName = it },
        label = { Text("New Task Name") }
    )
    Button(
        onClick = { viewModel.insertTask(PeclTaskEntity(0, newTaskName, poiId)) }, // Assume insertTask method in ViewModel/Repo
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text("Add Task")
    }
}

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Confirm Delete") },
        text = { Text("Delete this task?") },
        confirmButton = {
            Button(onClick = {
                selectedTask?.let { viewModel.deleteTask(it) } // Assume deleteTask method
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