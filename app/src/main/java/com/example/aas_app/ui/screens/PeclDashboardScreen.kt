package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.aas_app.data.entity.CommentEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.UserEntity
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
    val studentsState by viewModel.studentsState.observeAsState(AppState.Loading)
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

    LaunchedEffect(Unit) {
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
            (studentsState as AppState.Success).data.forEach { student ->
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
            val students = (studentsState as AppState.Success).data
            val tasks = (tasksState as AppState.Success).data
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
                    is AppState.Loading -> DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                    is AppState.Success<*> -> {
                        val instructors = (state as AppState.Success<List<UserEntity>>).data
                        instructors.forEach { instructor: UserEntity ->
                            DropdownMenuItem(
                                text = { Text(instructor.fullName) },
                                onClick = {
                                    selectedInstructor = instructor
                                    expandedInstructor = false
                                }
                            )
                        }
                    }
                    is AppState.Error -> DropdownMenuItem(text = { Text("Error: ${state.message}") }, onClick = {})
                }
            }
        }

        if (selectedInstructor != null) {
            when (val state = studentsState) {
                is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is AppState.Success<*> -> {
                    val students = (state as AppState.Success<List<PeclStudentEntity>>).data
                    if (students.isEmpty()) {
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
                                    .fillMaxWidth(0.3f)
                            )
                            when (val tasks = tasksState) {
                                is AppState.Loading -> Text(
                                    text = "Loading...",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                is AppState.Success<*> -> {
                                    val taskList = (tasks as AppState.Success<List<PeclTaskEntity>>).data
                                    taskList.forEach { task: PeclTaskEntity ->
                                        Text(
                                            text = task.name,
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .fillMaxWidth(0.15f)
                                        )
                                    }
                                }
                                is AppState.Error -> Text(
                                    text = "Error: ${tasks.message}",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }

                        LazyColumn {
                            items(students) { student: PeclStudentEntity ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = student.fullName, modifier = Modifier.fillMaxWidth(0.3f))
                                    LazyRow {
                                        when (val tasks = tasksState) {
                                            is AppState.Loading -> item { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
                                            is AppState.Success<*> -> {
                                                val taskList = (tasks as AppState.Success<List<PeclTaskEntity>>).data
                                                items(taskList) { task: PeclTaskEntity ->
                                                    val key = Pair(student.id, task.id)
                                                    val grade = taskGradesByStudentTask[key]
                                                    val displayGrade = grade?.toString() ?: "No Grade"
                                                    Text(
                                                        text = displayGrade,
                                                        modifier = Modifier
                                                            .fillMaxWidth(0.15f)
                                                            .clickable {
                                                                navController.navigate("grading/$programId/$poiId/${student.id}/${task.id}")
                                                            },
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            }
                                            is AppState.Error -> item { Text(
                                                text = "Error: ${tasks.message}",
                                                modifier = Modifier.fillMaxWidth(0.15f),
                                                textAlign = TextAlign.Center
                                            ) }
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
                is AppState.Error -> Text(
                    text = "Error: ${state.message}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (showComments && selectedStudentForComments != null) {
            AlertDialog(
                onDismissRequest = { showComments = false },
                title = { Text("Comments for ${selectedStudentForComments?.fullName}") },
                text = {
                    when (val state = commentsState) {
                        is AppState.Loading -> CircularProgressIndicator()
                        is AppState.Success<*> -> {
                            val commentList = (state as AppState.Success<List<CommentEntity>>).data
                            if (commentList.isEmpty()) {
                                Text("No comments available.")
                            } else {
                                LazyColumn {
                                    items(commentList) { comment: CommentEntity ->
                                        Text(
                                            text = "${comment.timestamp}: ${comment.comment}",
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