package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
modifier = Modifier.fillMaxSize().padding(16.dp),
verticalArrangement = Arrangement.Center,
horizontalAlignment = Alignment.CenterHorizontally
) {
    TextField(
        value = subTask,
        onValueChange = { subTask = it },
        label = { Text("Sub Task") }
    )
    TextField(
        value = controlType,
        onValueChange = { controlType = it },
        label = { Text("Control Type") }
    )
    TextField(
        value = scale,
        onValueChange = { scale = it },
        label = { Text("Scale") }
    )
    TextField(
        value = criticalTask,
        onValueChange = { criticalTask = it },
        label = { Text("Critical Task") }
    )
    Button(
        onClick = {
            val updatedQuestion = PeclQuestionEntity(questionId, subTask, controlType, scale, criticalTask)
            viewModel.updateQuestion(updatedQuestion, 0L)
            navController.popBackStack()
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text("Update")
    }
}
}