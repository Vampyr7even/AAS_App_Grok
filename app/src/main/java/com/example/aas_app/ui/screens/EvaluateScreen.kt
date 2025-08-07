package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.PeclViewModel

@Composable
fun EvaluateScreen(viewModel: PeclViewModel, selectedProgram: String, selectedPoi: String) {
    val questions by viewModel.questionsForPoi.collectAsState(initial = emptyList())
    val tasks by viewModel.peclTasks.collectAsState(initial = emptyList())
    val groupedQuestions = questions.groupBy { question ->
        tasks.find { it.id == question.taskId }?.peclTask ?: "Unknown" // Assume taskId in PeclQuestionEntity
    }
    val responses = remember { mutableStateMapOf<Int, String>() }
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedProgram, selectedPoi) {
        viewModel.loadQuestionsForPoi(selectedProgram, selectedPoi)
        isLoading = false
    }

    if (isLoading) {
        Text("Loading questions...", modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            groupedQuestions.entries.forEach { (task, subtasks) ->
                item {
                    Text(text = task, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(vertical = 8.dp))
                }
                items(subtasks.size) { index ->
                    val question = subtasks[index]
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(text = question.subTask)
                        var response by remember { mutableStateOf(responses[question.id] ?: "") }
                        when (question.controlType) {
                            "TextBox" -> TextField(
                                value = response,
                                onValueChange = { newResponse ->
                                    response = newResponse
                                    responses[question.id] = newResponse
                                },
                                label = { Text("Enter response") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Add cases for ComboBox, ScoreBox, Comment, etc.
                            else -> Text("Unsupported: ${question.controlType}")
                        }
                    }
                }
            }
        }

        Button(
            onClick = { /* Save responses to results, handle errors if fail */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}