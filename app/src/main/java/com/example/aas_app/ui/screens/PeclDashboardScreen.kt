package com.example.aas_app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.*
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeclDashboardScreen(navController: NavController, programId: Long, poiId: Long) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val instructorsState by viewModel.instructorsState.observeAsState(AppState.Loading)
    val studentsState by viewModel.studentsForInstructorAndProgramState.observeAsState(AppState.Loading)
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading)
    val commentsState by viewModel.commentsState.observeAsState(AppState.Loading)

    var selectedInstructor by remember { mutableStateOf<UserEntity?>(null) }
    var selectedStudentForComments by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var showComments by remember { mutableStateOf(false) }
    var expandedInstructor by remember { mutableStateOf(false) }

    LaunchedEffect(poiId) {
        viewModel.loadInstructors()
        viewModel.loadTasksForPoi(poiId)
    }

    LaunchedEffect(selectedInstructor) {
        selectedInstructor?.let {
            viewModel.loadStudentsForInstructorAndProgram(it.id, programId)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Instructor Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedInstructor,
            onExpandedChange = { expandedInstructor = !expandedInstructor }
        ) {
            TextField(
                readOnly = true,
                value = selectedInstructor?.fullName ?: "",
                onValueChange = { },
                label = { Text("Select Instructor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstructor) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedInstructor,
                onDismissRequest = { expandedInstructor = false }
            ) {
                when (val state = instructorsState) {
                    is AppState.Loading -> DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                    is AppState.Success -> state.data.forEach { instructor ->
                        DropdownMenuItem(
                            text = { Text(instructor.fullName) },
                            onClick = {
                                selectedInstructor = instructor
                                expandedInstructor = false
                            }
                        )
                    }
                    is AppState.Error -> DropdownMenuItem(text = { Text("Error: ${state.message}") }, onClick = {})
                }
            }
        }

        if (selectedInstructor != null) {
            when (val students = studentsState) {
                is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is AppState.Success -> {
                    // Header Row: Tasks
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
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
                                LazyRow {
                                    when (val tasks = tasksState) {
                                        is AppState.Success -> items(tasks.data) { task ->
                                            Button(
                                                onClick = { navController.navigate("grading/${student.id}/${task.id}") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier
                                                    .width(100.dp)
                                                    .padding(end = 8.dp)
                                            ) {
                                                Text("Grade")
                                            }
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
                is AppState.Error -> Text("Error: ${students.message}", textAlign = TextAlign.Center)
            }
        }

        if (showComments) {
            AlertDialog(
                onDismissRequest = { showComments = false },
                title = { Text("Comments for ${selectedStudentForComments?.fullName}") },
                text = {
                    when (val state = commentsState) {
                        is AppState.Loading -> CircularProgressIndicator()
                        is AppState.Success -> LazyColumn {
                            items(state.data) { comment ->
                                Text(comment)
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
    }
}