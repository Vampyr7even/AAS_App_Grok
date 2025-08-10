package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
modifier = Modifier.fillMaxSize().padding(16.dp),
verticalArrangement = Arrangement.Center,
horizontalAlignment = Alignment.CenterHorizontally
) {
    when (val state = questionsState) {
        is AppState.Loading -> Text("Loading...")
        is AppState.Success -> {
            LazyColumn {
                items(state.data) { question ->
                    Row {
                        Text(question.subTask)
                        IconButton(onClick = { navController.navigate("edit_question/${question.id}") }) { // Assume route added
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { selectedQuestion = question; showDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
        is AppState.Error -> Text("Error: ${state.message}")
    }

    var newSubTask by remember { mutableStateOf("") }
    // Add more fields as needed
    TextField(
        value = newSubTask,
        onValueChange = { newSubTask = it },
        label = { Text("New Sub Task") }
    )
    Button(
        onClick = { viewModel.insertQuestion(PeclQuestionEntity(0, newSubTask, "Text", "Scale", "No"), 0L) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text("Add Question")
    }
}

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Confirm Delete") },
        text = { Text("Delete this question?") },
        confirmButton = {
            Button(onClick = {
                selectedQuestion?.let { viewModel.deleteQuestion(it, 0L) }
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