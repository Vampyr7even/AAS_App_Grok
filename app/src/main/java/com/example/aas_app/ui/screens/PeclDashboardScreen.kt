package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.*
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeclDashboardScreen(
    navController: NavController,
    programId: Long,
    poiId: Long,
    instructorId: Long
) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val instructorsState by viewModel.instructorsState.observeAsState(AppState.Loading)
    val studentsState by viewModel.studentsForInstructorAndProgramState.observeAsState(AppState.Loading)
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading)
    val evaluationResultsState by viewModel.evaluationResultsState.observeAsState(AppState.Loading)
    val commentsState by viewModel.commentsState.observeAsState(AppState.Loading)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedInstructor by remember { mutableStateOf<UserEntity?>(null) }
    var selectedStudentForComments by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var showComments by remember { mutableStateOf(false) }
    var expandedInstructor by remember { mutableStateOf(false) }

    val horizontalScrollState = rememberScrollState()

    // Per-student evaluations map for live updates
    val evaluationsByStudent = remember { mutableStateMapOf<Long, AppState<List<PeclEvaluationResultEntity>>>() }

    // Per-student-task grades map for task_grade
    val taskGradesByStudentTask = remember { mutableStateMapOf<Pair<Long, Long>, Double?>() }

    // Initialize selectedInstructor with instructorId
    LaunchedEffect(instructorId) {
        viewModel.loadInstructors()
        viewModel.loadTasksForPoi(poiId)
        if (instructorId != 0L) {
            viewModel.loadStudentsForInstructorAndProgram(instructorId, programId)
            viewModel.getInstructorById(instructorId) { instructor ->
                selectedInstructor = instructor
            }
        }
    }

    // Update students when instructor changes via dropdown
    LaunchedEffect(selectedInstructor) {
        selectedInstructor?.let {
            if (it.id != instructorId) { // Only reload if different from initial
                viewModel.loadStudentsForInstructorAndProgram(it.id, programId)
            }
        }
    }

    // Load evaluation results for students with live collection
    LaunchedEffect(studentsState) {
        if (studentsState is AppState.Success) {
            (studentsState as AppState.Success).data.forEach { student ->
                evaluationsByStudent[student.id] = AppState.Loading
                viewModel.loadEvaluationResultsForStudent(student.id) // Initial load if needed
                viewModel.getEvaluationsForStudent(student.id).collect { data ->
                    evaluationsByStudent[student.id] = AppState.Success(data)
                }
            }
        }
    }

    // Load task grades for each student-task pair
    LaunchedEffect(studentsState, tasksState) {
        if (studentsState is AppState.Success && tasksState is AppState.Success) {
            val students = (studentsState as AppState.Success).data
            val tasks = (tasksState as AppState.Success).data
            students.forEach { student ->
                tasks.forEach { task ->
                    val key = Pair(student.id, task.id)
                    taskGradesByStudentTask[key] = null // Initial null
                    viewModel.loadTaskGradeForStudent(student.id, task.id).collect { grade ->
                        taskGradesByStudentTask[key] = grade
                    }
                }
            }
        }
    }

    // Handle instructor error state
    LaunchedEffect(instructorsState) {
        if (instructorsState is AppState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Error loading instructors: ${(instructorsState as AppState.Error).message}"
                )
            }
        }
    }

    // Handle comments error state
    LaunchedEffect(commentsState) {
        if (commentsState is AppState.Error) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Error loading comments: ${(commentsState as AppState.Error).message}"
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Instructor Dropdown (for switching instructors)
        ExposedDropdownMenuBox(
            expanded = expandedInstructor,
            onExpandedChange = { expandedInstructor = !expandedInstructor }
        ) {
            TextField(
                readOnly = true,
                value = selectedInstructor?.fullName ?: "Select Instructor",
                onValueChange = { },
                label = { Text("Select Instructor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstructor) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedContainerColor = Color(0xFFE57373),
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFFE57373),
                    unfocusedIndicatorColor = Color.Black
                ),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedInstructor,
                onDismissRequest = { expandedInstructor = false }
            ) {
                when (val state = instructorsState) {
                    is AppState.Loading -> DropdownMenuItem(
                        text = { Text("Loading...") },
                        onClick = {}
                    )
                    is AppState.Success -> state.data.forEach { instructor ->
                        DropdownMenuItem(
                            text = { Text(instructor.fullName) },
                            onClick = {
                                selectedInstructor = instructor
                                expandedInstructor = false
                            }
                        )
                    }
                    is AppState.Error -> DropdownMenuItem(
                        text = { Text("Error: ${state.message}") },
                        onClick = {}
                    )
                }
            }
        }

        // Dashboard Table
        if (selectedInstructor != null) {
            when (val students = studentsState) {
                is AppState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                is AppState.Success -> {
                    if (students.data.isEmpty()) {
                        Text(
                            text = "No students assigned to this instructor.",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Header Row: Tasks with shared scroll state
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(horizontalScrollState)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "Students / Tasks",
                                modifier = Modifier
                                    .width(150.dp)
                                    .padding(end = 8.dp)
                            )
                            when (val tasks = tasksState) {
                                is AppState.Success -> tasks.data.forEach { task ->
                                    Text(
                                        text = task.name,
                                        modifier = Modifier
                                            .width(100.dp)
                                            .padding(end = 8.dp)
                                    )
                                }
                                is AppState.Error -> Text(
                                    text = "Error: ${tasks.message}",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                else -> {}
                            }
                        }

                        // Student Rows
                        LazyColumn {
                            items(students.data) { student ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = student.fullName,
                                        modifier = Modifier
                                            .width(150.dp)
                                            .padding(end = 8.dp)
                                    )
                                    Row(
                                        modifier = Modifier.horizontalScroll(horizontalScrollState)
                                    ) {
                                        when (val tasks = tasksState) {
                                            is AppState.Success -> tasks.data.forEach { task ->
                                                val key = Pair(student.id, task.id)
                                                val grade = taskGradesByStudentTask[key]
                                                val displayGrade = grade?.toString() ?: "No Grade"
                                                Button(
                                                    onClick = {
                                                        navController.navigate(
                                                            "grading/$programId/$poiId/${student.id}/${task.id}"
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier
                                                        .width(100.dp)
                                                        .padding(end = 8.dp)
                                                ) {
                                                    Text(displayGrade)
                                                }
                                            }
                                            is AppState.Error -> {
                                                Text("Error: ${tasks.message}")
                                            }
                                            else -> {}
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            selectedStudentForComments = student
                                            showComments = true
                                            viewModel.loadCommentsForStudent(student.id)
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
                is AppState.Error -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error loading students: ${students.message}")
                    }
                    Text(
                        text = "Error: ${students.message}",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Comments Dialog
        if (showComments && selectedStudentForComments != null) {
            AlertDialog(
                onDismissRequest = { showComments = false },
                title = { Text("Comments for ${selectedStudentForComments?.fullName}") },
                text = {
                    when (val state = commentsState) {
                        is AppState.Loading -> CircularProgressIndicator()
                        is AppState.Success -> {
                            if (state.data.isEmpty()) {
                                Text("No comments available.")
                            } else {
                                LazyColumn {
                                    items(state.data) { comment ->
                                        Text(
                                            text = comment,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error: ${state.message}")
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showComments = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Close")
                    }
                }
            )
        }

        // Snackbar for errors
        SnackbarHost(hostState = snackbarHostState)
    }
}