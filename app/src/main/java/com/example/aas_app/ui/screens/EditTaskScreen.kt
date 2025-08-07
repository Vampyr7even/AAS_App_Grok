package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aas_app.data.Result
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.PeclViewModel

@Composable
fun EditTaskScreen(viewModel: PeclViewModel, taskId: Int) {
    var task by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var taskName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(taskId) {
        val result = viewModel.getPeclTaskById(taskId)
        if (result is Result.Success) {
            task = result.data
            taskName = result.data?.peclTask ?: ""
        }
        isLoading = false
    }

    if (isLoading) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    if (task == null) {
        Text("Task not found", modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = taskName, onValueChange = { newValue -> taskName = newValue }, label = { Text("Task Name") })

        Button(onClick = {
            val updatedTask = task!!.copy(peclTask = taskName)
            viewModel.updatePeclTask(updatedTask)
        }) {
            Text("Update Task")
        }
    }
}