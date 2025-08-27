package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingScreen(navController: NavController, studentId: Long, taskId: Long) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading as AppState<List<PeclQuestionEntity>>)

    var scores by remember { mutableStateOf(mapOf<Long, Double>()) }
    var comments by remember { mutableStateOf(mapOf<Long, String>()) }

    LaunchedEffect(taskId) {
        viewModel.loadQuestionsForTask(taskId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = questionsState) {
            is AppState.Loading -> Text("Loading subtasks...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { question ->
                        Text(question.subTask)
                        // Dynamic control based on question.controlType (e.g., ScoreBox for score input)
                        TextField(
                            value = scores[question.id]?.toString() ?: "",
                            onValueChange = { scores = scores + mapOf(question.id to (it.toDoubleOrNull() ?: 0.0)) },
                            label = { Text("Score") }
                        )
                        TextField(
                            value = comments[question.id] ?: "",
                            onValueChange = { comments = comments + mapOf(question.id to it) },
                            label = { Text("Comment") }
                        )
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        Button(
            onClick = {
                scores.forEach { (questionId, score) ->
                    viewModel.insertEvaluationResult(PeclEvaluationResultEntity(student_id = studentId, instructor_id = 0L /* Current instructor */, question_id = questionId, score = score, comment = comments[questionId] ?: "", timestamp = System.currentTimeMillis()))
                }
                navController.popBackStack() // Return to dashboard
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Save Grades")
        }
    }
}