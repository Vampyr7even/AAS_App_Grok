package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val studentsState by viewModel.studentsState.observeAsState(AppState.Success(emptyList()))
    val tasksState by viewModel.tasksState.observeAsState(AppState.Success(emptyList()))
    val evaluationsState by viewModel.evaluationsForStudentAndTaskState.observeAsState(AppState.Success(emptyList<PeclEvaluationResultEntity>()))
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // State for student and task names
    var studentName by remember { mutableStateOf("Loading...") }
    var taskName by remember { mutableStateOf("Loading...") }

    // State for responses
    val responses = remember { mutableStateMapOf<Long, String>() } // questionId -> response
    var comment by remember { mutableStateOf("") }
    var taskGradeInput by remember { mutableStateOf("") } // New: Input for task_grade

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
        if (evaluationsState is AppState.Success) {
            val evals = (evaluationsState as AppState.Success).data
            evals.forEach { eval ->
                // Map score back to response string (adjust based on scale; here, simple numeric or YES/NO/N/A)
                responses[eval.question_id] = when (eval.score) {
                    1.0 -> "YES"
                    0.0 -> "NO"
                    -1.0 -> "N/A"
                    else -> eval.score.toString()
                }
            }
            val firstEval = evals.firstOrNull()
            comment = firstEval?.comment ?: ""
            taskGradeInput = firstEval?.task_grade?.toString() ?: ""
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
            is AppState.Loading -> CircularProgressIndicator()
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
                                            if ("N/A" !in options) options += "N/A" // Add N/A if not present
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
                                                        Text(option)
                                                    }
                                                }
                                            }
                                        } ?: Text("Loading scale...")
                                    }
                                    "ComboBox", "ScoreBox" -> {
                                        scale.value?.let { scaleEntity ->
                                            var options = scaleEntity.options.split(",")
                                            if ("N/A" !in options) options += "N/A" // Add N/A if not present
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
