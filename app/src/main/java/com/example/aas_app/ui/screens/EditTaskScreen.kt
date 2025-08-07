package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entities.PeclTaskEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(navController: NavController, taskId: Long) {
    val viewModel: AdminViewModel = hiltViewModel()
    val taskState by viewModel.tasksState.observeAsState(AppState.Loading<List<PeclTaskEntity>>())

    LaunchedEffect(taskId) {
        viewModel.loadTaskById(taskId) // Assume added to ViewModel/Repo: getTaskById
    }

    var taskName by remember { mutableStateOf("") }

    when (val state = taskState) {
        is AppState.Success -> {
            val task = state.data.firstOrNull { it.id == taskId } ?: return
            taskName = task.name
        }
        else -> { }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") }
        )
        Button(
            onClick = {
                val updatedTask = PeclTaskEntity(taskId, taskName, 0L) // Adjust poiId
                viewModel.updateTask(updatedTask)
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Update")
        }
    }
}