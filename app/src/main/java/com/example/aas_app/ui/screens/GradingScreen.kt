package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingScreen(
    navController: NavController,
    studentId: Long,
    taskId: Long,
    instructorId: Long,
    programId: Long,
    poiId: Long,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Success(emptyList()))
    val context = LocalContext.current
    val grades = remember { mutableStateMapOf<Long, String>() }
    val comments = remember { mutableStateMapOf<Long, String>() }

    LaunchedEffect(Unit) {
        viewModel.loadQuestionsForTask(taskId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Grade Task",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = questionsState) {
            is AppState.Loading -> CircularProgressIndicator()
            is AppState.Success -> {
                if (state.data.isEmpty()) {
                    Text("No questions available for this task")
                } else {
                    LazyColumn {
                        items(state.data) { question ->
                            QuestionGradingItem(
                                question = question,
                                grades = grades,
                                comments = comments,
                                viewModel = viewModel,
                                coroutineScope = coroutineScope,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        grades.forEach { (questionId, score) ->
                            val comment = comments[questionId] ?: ""
                            val scoreValue = score.toDoubleOrNull() ?: 0.0
                            viewModel.insertEvaluationResult(
                                PeclEvaluationResultEntity(
                                    student_id = studentId,
                                    instructor_id = instructorId,
                                    task_id = taskId,
                                    question_id = questionId,
                                    score = scoreValue,
                                    comment = comment,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        }
                        Toast.makeText(context, "Grades saved successfully", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } catch (e: Exception) {
                        Log.e("GradingScreen", "Error saving grades: ${e.message}", e)
                        snackbarHostState.showSnackbar("Error saving grades: ${e.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Check, contentDescription = "Save Grades")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Grades")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancel")
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionGradingItem(
    question: PeclQuestionEntity,
    grades: MutableMap<Long, String>,
    comments: MutableMap<Long, String>,
    viewModel: PeclViewModel,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    var selectedGrade by remember { mutableStateOf(grades[question.id] ?: "") }
    var commentText by remember { mutableStateOf(comments[question.id] ?: "") }
    var scaleOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(question.scale) {
        if (question.controlType in listOf("ComboBox", "ListBox") && question.scale.isNotBlank()) {
            try {
                viewModel.getScaleByName(question.scale) { scale ->
                    scaleOptions = scale?.options?.split(",")?.map { it.trim() } ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("QuestionGradingItem", "Error loading scale: ${e.message}", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading scale: ${e.message}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = question.subTask,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        when (question.controlType) {
            "ComboBox", "ListBox" -> {
                if (scaleOptions.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            value = selectedGrade,
                            onValueChange = { },
                            label = { Text("Select Grade") },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            scaleOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        selectedGrade = option
                                        grades[question.id] = option
                                        expanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text("No scale options available", style = MaterialTheme.typography.bodySmall)
                }
            }
            "TextBox" -> {
                TextField(
                    value = selectedGrade,
                    onValueChange = {
                        selectedGrade = it
                        grades[question.id] = it
                    },
                    label = { Text("Enter Grade") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            "CheckBox" -> {
                Checkbox(
                    checked = selectedGrade == "Checked",
                    onCheckedChange = {
                        selectedGrade = if (it) "Checked" else "Unchecked"
                        grades[question.id] = selectedGrade
                    }
                )
            }
            "ScoreBox" -> {
                TextField(
                    value = selectedGrade,
                    onValueChange = {
                        if (it.matches(Regex("\\d*\\.?\\d*"))) {
                            selectedGrade = it
                            grades[question.id] = it
                        }
                    },
                    label = { Text("Enter Score") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = commentText,
            onValueChange = {
                commentText = it
                comments[question.id] = it
            },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
    }
}