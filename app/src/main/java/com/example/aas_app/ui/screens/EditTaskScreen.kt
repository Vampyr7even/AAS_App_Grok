package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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