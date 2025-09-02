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
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // State for student and task names
    var studentName by remember { mutableStateOf("Loading...") }
    var taskName by remember { mutableStateOf("Loading...") }

    // State for responses
    val responses = remember { mutableStateMapOf<Long, String>() } // questionId -> response
    var comment by remember { mutableStateOf("") }

    // Load data
    LaunchedEffect(Unit) {
        try {
            viewModel.loadQuestionsForTask(taskId)
            viewModel.loadStudents()
            viewModel.loadAllTasks()
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
                                            val options = scaleEntity.options.split(",")
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
                                            val options = scaleEntity.options.split(",")
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
                                                    label = { Text("Select Option") },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
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
                                        } ?: Text("Loading scale...")
                                    }
                                    "TextBox" -> {
                                        TextField(
                                            value = responses[question.id] ?: "",
                                            onValueChange = { responses[question.id] = it },
                                            label = { Text("Enter Response") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    else -> Text("Unsupported control type: ${question.controlType}")
                                }
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        // Comment Section
        TextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Comments") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        // Save and Cancel Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    if (responses.isNotEmpty()) {
                        coroutineScope.launch {
                            try {
                                responses.forEach { (questionId, response) ->
                                    // Parse score from response (e.g., YES/NO/N/A or text)
                                    val score = when (response) {
                                        "YES" -> 1.0
                                        "NO" -> 0.0
                                        "N/A" -> -1.0
                                        else -> response.toDoubleOrNull() ?: 0.0 // For text or numeric inputs
                                    }
                                    val result = PeclEvaluationResultEntity(
                                        student_id = studentId,
                                        instructor_id = 1L, // Replace with actual instructor ID from context
                                        question_id = questionId,
                                        score = score,
                                        comment = comment,
                                        timestamp = System.currentTimeMillis(),
                                        task_id = taskId
                                    )
                                    viewModel.insertEvaluationResult(result)
                                }
                                Toast.makeText(context, "Grades saved successfully", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            } catch (e: Exception) {
                                Log.e("GradingScreen", "Error saving grades: ${e.message}", e)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error saving grades: ${e.message}")
                                }
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("No responses provided")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Save")
            }
        }

        // Snackbar for errors
        SnackbarHost(hostState = snackbarHostState)
    }
}