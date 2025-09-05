package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(navController: NavController, taskId: Long, poiId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    var taskName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(taskId) {
        val task = viewModel.getTaskById(taskId)
        task?.let {
            taskName = it.name
        } ?: run {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading task")
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
            text = "Edit Task",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (taskName.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            viewModel.updateTask(
                                PeclTaskEntity(id = taskId, name = taskName),
                                listOf(poiId)
                            )
                            snackbarHostState.showSnackbar("Task updated successfully")
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("EditTaskScreen", "Error updating task: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error updating task: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Task name cannot be blank")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Task")
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}