package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.AppResult
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingScreen(navController: NavController, studentId: Long, taskId: Long) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading)
    val coroutineScope = rememberCoroutineScope()

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
            is AppState.Loading -> Text("Loading questions...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { question ->
                        Text(question.subTask)
                        when (question.controlType) {
                            "ScoreBox" -> {
                                TextField(
                                    value = scores[question.id]?.toString() ?: "",
                                    onValueChange = { scores = scores + (question.id to (it.toDoubleOrNull() ?: 0.0)) },
                                    label = { Text("Score") }
                                )
                            }
                            "ComboBox" -> {
                                var expanded by remember { mutableStateOf(false) }
                                var selectedOption by remember { mutableStateOf("") }
                                val options = question.scale.split(",").map { it.trim() }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    TextField(
                                        readOnly = true,
                                        value = selectedOption,
                                        onValueChange = { },
                                        label = { Text("Select Option") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                        modifier = Modifier.menuAnchor().fillMaxWidth()
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        options.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    selectedOption = option
                                                    expanded = false
                                                    comments = comments + (question.id to option)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            "Comment", "TextBox" -> {
                                TextField(
                                    value = comments[question.id] ?: "",
                                    onValueChange = { comments = comments + (question.id to it) },
                                    label = { Text("Comment") }
                                )
                            }
                            else -> Text("Unsupported control type: ${question.controlType}")
                        }
                    }
                }
                Button(
                    onClick = {
                        coroutineScope.launch {
                            scores.forEach { (questionId, score) ->
                                val result = PeclEvaluationResultEntity(
                                    student_id = studentId,
                                    instructor_id = 1L, // Placeholder; replace with current instructor
                                    question_id = questionId,
                                    score = score,
                                    comment = comments[questionId] ?: "",
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.insertEvaluationResult(result)
                            }
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Save Grades")
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }
    }
}