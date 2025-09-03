package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingScreen(
    navController: NavController,
    programId: Long,
    poiId: Long,
    studentId: Long,
    taskId: Long
) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Success(emptyList<PeclQuestionEntity>()))
    val studentsState by viewModel.studentsState.observeAsState(AppState.Success(emptyList<PeclStudentEntity>()))
    val tasksState by viewModel.tasksState.observeAsState(AppState.Success(emptyList<PeclTaskEntity>()))
    val evaluationsState by viewModel.evaluationsForStudentAndTaskState.observeAsState(AppState.Success(emptyList<PeclEvaluationResultEntity>()))
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // State for student and task names
    var studentName by remember { mutableStateOf("Loading...") }
    var taskName by remember { mutableStateOf("Loading...") }

    // State for responses
    val responses = remember { mutableStateMapOf<Long, String>() } // questionId -> response

    // Multi-comment support: List of comments
    val comments = remember { mutableStateListOf<String>() }

    var taskGradeInput by remember { mutableStateOf("") } // Input for task_grade

    // Load data
    LaunchedEffect(Unit) {
        try {
            viewModel.loadQuestionsForTask(taskId)
            viewModel.loadStudents()
            viewModel.loadAllTasks()
            viewModel.loadEvaluationsForStudentAndTask(studentId, taskId)
            // Fetch student name
            viewModel.getPeclStudentById(studentId) { student: PeclStudentEntity? ->
                studentName = student?.fullName ?: "Unknown Student"
            }
            // Fetch task name
            viewModel.getTaskById(taskId) { task: PeclTaskEntity? ->
                taskName = task?.name ?: "Unknown Task"
            }
        } catch (e: Exception) {
            Log.e("GradingScreen", "Error loading initial data: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading data: ${e.message}")
            }
        }
    }

    // Pre-populate from existing evaluations
    LaunchedEffect(evaluationsState) {
        when (evaluationsState) {
            is AppState.Success -> {
                val evals = (evaluationsState as AppState.Success).data
                evals.forEach { eval ->
                    // Map score back to response string
                    responses[eval.question_id] = when (eval.score) {
                        1.0 -> "YES"
                        0.0 -> "NO"
                        -1.0 -> "N/A"
                        else -> eval.score.toString()
                    }
                }
                val firstEval = evals.firstOrNull()
                // Split concatenated comments
                comments.clear()
                comments.addAll((firstEval?.comment ?: "").split("\n---\n").filter { it.isNotBlank() })
                taskGradeInput = firstEval?.task_grade?.toString() ?: ""
            }
            is AppState.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading evaluations: ${(evaluationsState as AppState.Error).message}")
                }
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title with student and task names
        Text(
            text = "Grading $studentName for $taskName",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = questionsState) {
            is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is AppState.Success -> {
                if (state.data.isEmpty()) {
                    Text("No sub-tasks available for this task.")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(state.data) { question ->
                            val scale = remember { mutableStateOf<ScaleEntity?>(null) }
                            LaunchedEffect(question.id) {
                                try {
                                    val scaleData = viewModel.getScaleByName(question.scale)
                                    scale.value = scaleData
                                } catch (e: Exception) {
                                    Log.e("GradingScreen", "Error loading scale for question ${question.subTask}: ${e.message}", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error loading scale: ${e.message}")
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
                                    "OptionButton" -> {
                                        scale.value?.let { scaleEntity ->
                                            var options = scaleEntity.options.split(",")
                                            if ("N/A" !in options) options += "N/A"
                                            var selectedOption by remember { mutableStateOf(responses[question.id] ?: "") }
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                options.forEach { option ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        RadioButton(
                                                            selected = selectedOption == option,
                                                            onClick = {
                                                                selectedOption = option
                                                                responses[question.id] = option
                                                            }
                                                        )
                                                        Text(text = option)
                                                    }
                                                }
                                            }
                                        } ?: Text(text = "Loading scale...")
                                    }
                                    "ComboBox", "ScoreBox" -> {
                                        scale.value?.let { scaleEntity ->
                                            var options = scaleEntity.options.split(",")
                                            if ("N/A" !in options) options += "N/A"
                                            var expanded by remember { mutableStateOf(false) }
                                            var selectedOption by remember { mutableStateOf(responses[question.id] ?: options[0]) }
                                            ExposedDropdownMenuBox(
                                                expanded = expanded,
                                                onExpandedChange = { expanded = !expanded }
                                            ) {
                                                TextField(
                                                    readOnly = true,
                                                    value = selectedOption,
                                                    onValueChange = { },
                                                    label = { Text(question.subTask) },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                                    modifier = Modifier.menuAnchor()
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
                                                                responses[question.id] = option
                                                                expanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        } ?: Text(text = "Loading scale...")
                                    }
                                    "TextBox", "Comment" -> {
                                        TextField(
                                            value = responses[question.id] ?: "",
                                            onValueChange = { responses[question.id] = it },
                                            label = { Text(question.subTask) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    else -> Text(text = "Unsupported control type: ${question.controlType}")
                                }
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text(
                text = "Error: ${state.message}",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Multi-comment section (Phase 5)
        Text(
            text = "Comments",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        LazyColumn {
            itemsIndexed(comments) { index: Int, commentItem: String ->
                TextField(
                    value = commentItem,
                    onValueChange = { newValue ->
                        comments[index] = newValue
                    },
                    label = { Text("Comment ${index + 1}") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
        }
        Button(
            onClick = { comments.add("") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Comment")
            Text("Add New Comment")
        }

        TextField(
            value = taskGradeInput,
            onValueChange = { taskGradeInput = it },
            label = { Text("Task Grade") },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        )

        Button(
            onClick = {
                if (taskGradeInput.toDoubleOrNull() == null && taskGradeInput.isNotBlank()) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Invalid task grade: must be a number")
                    }
                    return@Button
                }
                coroutineScope.launch {
                    try {
                        viewModel.deleteEvaluationsForStudentAndTask(studentId, taskId)
                        val concatenatedComments = comments.filter { it.isNotBlank() }.joinToString("\n---\n")
                        if (questionsState is AppState.Success) {
                            (questionsState as AppState.Success).data.forEach { question ->
                                val response = responses[question.id] ?: ""
                                val score = when (response) {
                                    "YES" -> 1.0
                                    "NO" -> 0.0
                                    "N/A" -> -1.0
                                    else -> response.toDoubleOrNull() ?: 0.0
                                }
                                val eval = PeclEvaluationResultEntity(
                                    student_id = studentId,
                                    instructor_id = 1L, // Replace with actual instructor ID (Phase 6)
                                    question_id = question.id,
                                    score = score,
                                    comment = concatenatedComments,
                                    timestamp = System.currentTimeMillis(),
                                    task_id = taskId,
                                    task_grade = taskGradeInput.toDoubleOrNull()
                                )
                                viewModel.insertEvaluationResult(eval)
                            }
                            snackbarHostState.showSnackbar("Grades and comments saved successfully")
                        } else {
                            snackbarHostState.showSnackbar("Cannot save: Questions not loaded")
                        }
                    } catch (e: Exception) {
                        Log.e("GradingScreen", "Error saving evaluations: ${e.message}", e)
                        snackbarHostState.showSnackbar("Error saving: ${e.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Save")
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}