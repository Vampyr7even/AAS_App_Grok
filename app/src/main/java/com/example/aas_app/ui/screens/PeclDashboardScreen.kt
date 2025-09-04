package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
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

    val evaluationsByStudent = remember { mutableStateMapOf<Long, AppState<List<PeclEvaluationResultEntity>>>() }

    val taskGradesByStudentTask = remember { mutableStateMapOf<Pair<Long, Long>, Double?>() }

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

    LaunchedEffect(selectedInstructor) {
        selectedInstructor?.let {
            if (it.id != instructorId) {
                viewModel.loadStudentsForInstructorAndProgram(it.id, programId)
            }
        }
    }

    LaunchedEffect(studentsState) {
        if (studentsState is AppState.Success) {
            (studentsState as AppState.Success<List<PeclStudentEntity>>).data.forEach { student ->
                evaluationsByStudent[student.id] = AppState.Loading
                viewModel.loadEvaluationResultsForStudent(student.id)
                viewModel.getEvaluationsForStudent(student.id).collect { data ->
                    evaluationsByStudent[student.id] = AppState.Success(data)
                }
            }
        }
    }

    LaunchedEffect(studentsState, tasksState) {
        if (studentsState is AppState.Success && tasksState is AppState.Success) {
            val students = (studentsState as AppState.Success<List<PeclStudentEntity>>).data
            val tasks = (tasksState as AppState.Success<List<PeclTaskEntity>>).data
            students.forEach { student ->
                tasks.forEach { task ->
                    val key = Pair(student.id, task.id)
                    taskGradesByStudentTask[key] = null
                    viewModel.getTaskGradeForStudent(student.id, task.id).collect { grade ->
                        taskGradesByStudentTask[key] = grade
                    }
                }
            }
        }
    }

    LaunchedEffect(instructorsState) {
        if (instructorsState is AppState.Error) {
            Log.w("PeclDashboardScreen", "Instructor load error: ${(instructorsState as AppState.Error).message}")
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Error loading instructors: ${(instructorsState as AppState.Error).message}"
                )
            }
        }
    }

    LaunchedEffect(commentsState) {
        if (commentsState is AppState.Error) {
            Log.w("PeclDashboardScreen", "Comments load error: ${(commentsState as AppState.Error).message}")
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Error loading comments: ${(commentsState as AppState.Error).message}"
                )
            }
        }
    }

    LaunchedEffect(studentsState) {
        if (studentsState is AppState.Error) {
            Log.w("PeclDashboardScreen", "Students load error: ${(studentsState as AppState.Error).message}")
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading students: ${(studentsState as AppState.Error).message}")
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    is AppState.Success<List<UserEntity>> -> state.data.forEach { instructor ->
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

        if (selectedInstructor != null) {
            when (val students = studentsState) {
                is AppState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                is AppState.Success<List<PeclStudentEntity>> -> {
                    if (students.data.isEmpty()) {
                        Text(
                            text = "No students assigned to this instructor.",
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
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
                                    .padding(end = 8.dp)
                                    .width(150.dp)
                            )
                            when (val tasks = tasksState) {
                                is AppState.Success<List<PeclTaskEntity>> -> tasks.data.forEach { task ->
                                    Text(
                                        text = task.name,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .width(100.dp)
                                    )
                                }
                                is AppState.Error -> Text(
                                    text = "Error: ${tasks.message}",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                else -> {}
                            }
                        }

                        LazyColumn {
                            items(students.data) { student ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = student.fullName,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .width(150.dp)
                                    )
                                    Row(
                                        modifier = Modifier.horizontalScroll(horizontalScrollState)
                                    ) {
                                        when (val tasks = tasksState) {
                                            is AppState.Success<List<PeclTaskEntity>> -> tasks.data.forEach { task ->
                                                val key = Pair(student.id, task.id)
                                                val grade = taskGradesByStudentTask[key]
                                                val displayGrade = grade?.toString() ?: "No Grade"
                                                Button(
                                                    onClick = {
                                                        try {
                                                            Log.d("PeclDashboardScreen", "Navigating to grading/$programId/$poiId/${student.id}/${task.id}")
                                                            navController.navigate(
                                                                "grading/$programId/$poiId/${student.id}/${task.id}"
                                                            )
                                                        } catch (e: Exception) {
                                                            Log.e("PeclDashboardScreen", "Navigation error to grading: ${e.message}", e)
                                                            coroutineScope.launch {
                                                                snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                                    shape = RoundedCornerShape(4.dp),
                                                    modifier = Modifier
                                                        .padding(end = 8.dp)
                                                        .width(100.dp)
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
                    Text(
                        text = "Error: ${students.message}",
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (showComments && selectedStudentForComments != null) {
            AlertDialog(
                onDismissRequest = { showComments = false },
                title = { Text("Comments for ${selectedStudentForComments?.fullName}") },
                text = {
                    when (val state = commentsState) {
                        is AppState.Loading -> CircularProgressIndicator()
                        is AppState.Success<List<CommentEntity>> -> {
                            if (state.data.isEmpty()) {
                                Text("No comments available.")
                            } else {
                                LazyColumn {
                                    items(state.data) { comment ->
                                        Text(
                                            text = comment.comment,
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

        SnackbarHost(hostState = snackbarHostState)
    }
}