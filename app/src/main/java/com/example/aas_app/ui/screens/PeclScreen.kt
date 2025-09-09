package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.aas_app.data.entity.*
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PeclScreen(
    navController: NavController,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val instructorsState by viewModel.instructorsState.observeAsState(AppState.Loading)
    val programsState by viewModel.programsForInstructorState.observeAsState(AppState.Loading)
    val poisState by viewModel.poisState.observeAsState(AppState.Loading)
    val studentsState by viewModel.studentsForInstructorAndProgramState.observeAsState(AppState.Loading)
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading)
    val evaluationResultsState by viewModel.evaluationResultsState.observeAsState(AppState.Loading)
    val commentsState by viewModel.commentsState.observeAsState(AppState.Loading)

    var selectedInstructor by remember { mutableStateOf<UserEntity?>(null) }
    var selectedProgram by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var selectedPoi by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var selectedStudent by remember { mutableStateOf<PeclStudentEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        try {
            viewModel.loadInstructors()
        } catch (e: Exception) {
            Log.e("PeclScreen", "Error loading instructors: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading instructors: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "PECL Evaluation",
            style = MaterialTheme.typography.headlineMedium
        )

        // Instructor Selection
        DropdownSelector(
            label = "Select Instructor",
            items = (instructorsState as? AppState.Success<List<UserEntity>>)?.data ?: emptyList(),
            selectedItem = selectedInstructor,
            onItemSelected = { instructor ->
                selectedInstructor = instructor
                selectedProgram = null
                selectedPoi = null
                selectedStudent = null
                coroutineScope.launch {
                    try {
                        instructor?.id?.let { viewModel.loadProgramsForInstructor(it) }
                    } catch (e: Exception) {
                        Log.e("PeclScreen", "Error loading programs: ${e.message}", e)
                        snackbarHostState.showSnackbar("Error loading programs: ${e.message}")
                    }
                }
            },
            displayText = { it?.fullName ?: "Select Instructor" }
        )

        // Program Selection
        DropdownSelector(
            label = "Select Program",
            items = (programsState as? AppState.Success<List<PeclProgramEntity>>)?.data ?: emptyList(),
            selectedItem = selectedProgram,
            onItemSelected = { program ->
                selectedProgram = program
                selectedPoi = null
                selectedStudent = null
                coroutineScope.launch {
                    try {
                        program?.id?.let { viewModel.loadPoisForProgram(it) }
                    } catch (e: Exception) {
                        Log.e("PeclScreen", "Error loading POIs: ${e.message}", e)
                        snackbarHostState.showSnackbar("Error loading POIs: ${e.message}")
                    }
                }
            },
            displayText = { it?.name ?: "Select Program" },
            enabled = selectedInstructor != null
        )

        // POI Selection
        DropdownSelector(
            label = "Select POI",
            items = (poisState as? AppState.Success<List<PeclPoiEntity>>)?.data ?: emptyList(),
            selectedItem = selectedPoi,
            onItemSelected = { poi ->
                selectedPoi = poi
                selectedStudent = null
                coroutineScope.launch {
                    try {
                        poi?.id?.let { viewModel.loadTasksForPoi(it) }
                        if (selectedInstructor != null && selectedProgram != null) {
                            viewModel.loadStudentsForInstructorAndProgram(
                                selectedInstructor!!.id,
                                selectedProgram!!.id
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("PeclScreen", "Error loading tasks or students: ${e.message}", e)
                        snackbarHostState.showSnackbar("Error loading tasks or students: ${e.message}")
                    }
                }
            },
            displayText = { it?.name ?: "Select POI" },
            enabled = selectedProgram != null
        )

        // Student Selection
        DropdownSelector(
            label = "Select Student",
            items = (studentsState as? AppState.Success<List<PeclStudentEntity>>)?.data ?: emptyList(),
            selectedItem = selectedStudent,
            onItemSelected = { student ->
                selectedStudent = student
                coroutineScope.launch {
                    try {
                        student?.id?.let { viewModel.loadEvaluationResultsForStudent(it) }
                        student?.id?.let { viewModel.loadCommentsForStudent(it) }
                    } catch (e: Exception) {
                        Log.e("PeclScreen", "Error loading evaluations or comments: ${e.message}", e)
                        snackbarHostState.showSnackbar("Error loading evaluations or comments: ${e.message}")
                    }
                }
            },
            displayText = { it?.fullName ?: "Select Student" },
            enabled = selectedPoi != null
        )

        // Dashboard
        if (selectedStudent != null && selectedPoi != null) {
            when (val tasks = tasksState) {
                is AppState.Loading -> Text(text = "Loading tasks...")
                is AppState.Success -> {
                    if (tasks.data.isEmpty()) {
                        Text(text = "No tasks available for this POI.")
                    } else {
                        LazyColumn {
                            items(tasks.data) { task ->
                                val grade by viewModel.getTaskGradeForStudent(selectedStudent!!.id, task.id)
                                    .collectAsState(initial = null)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = task.name, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            text = "Grade: ${grade?.toString() ?: "Not graded"}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            navController.navigate("editQuestions/${task.id}")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Grade")
                                    }
                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    viewModel.loadCommentsForStudent(selectedStudent!!.id)
                                                    // Navigate to a comments screen or show dialog (not implemented here)
                                                    Toast.makeText(context, "View comments for ${task.name}", Toast.LENGTH_SHORT).show()
                                                } catch (e: Exception) {
                                                    Log.e("PeclScreen", "Error loading comments: ${e.message}", e)
                                                    snackbarHostState.showSnackbar("Error loading comments: ${e.message}")
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Comments")
                                    }
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text(text = "Error: ${tasks.message}")
            }
        }

        // Edit Programs Button
        Button(
            onClick = { navController.navigate("editPrograms") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Edit Programs")
        }
    }

    // Error Handling for States
    LaunchedEffect(instructorsState, programsState, poisState, studentsState, tasksState, evaluationResultsState, commentsState) {
        listOf(instructorsState, programsState, poisState, studentsState, tasksState, evaluationResultsState, commentsState).forEach { state ->
            if (state is AppState.Error) {
                snackbarHostState.showSnackbar("Error: ${state.message}")
            }
        }
    }

    SnackbarHost(hostState = snackbarHostState)
}

@Composable
fun <T : Any> DropdownSelector(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    displayText: (T?) -> String,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = displayText(selectedItem),
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = enabled,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Expand")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(displayText(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onItemSelected(null)
                    expanded = false
                }
            )
        }
    }
}