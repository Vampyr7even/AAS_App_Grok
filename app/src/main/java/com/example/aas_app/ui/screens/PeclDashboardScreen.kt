package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeclDashboardScreen(navController: NavController, poiId: Long) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val studentsState by viewModel.studentsState.observeAsState(AppState.Loading as AppState<List<PeclStudentEntity>>)
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading as AppState<List<PeclTaskEntity>>)
    val resultsState by viewModel.evaluationResultsState.observeAsState(AppState.Loading as AppState<List<PeclEvaluationResultEntity>>)
    val commentsState by viewModel.commentsState.observeAsState(AppState.Loading as AppState<List<String>>)

    var selectedStudent by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var showComments by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<PeclTaskEntity?>(null) } // for grading
    var expandedInstructor by remember { mutableStateOf(false) }
    var selectedInstructorName by remember { mutableStateOf("") }

    LaunchedEffect(poiId) {
        viewModel.loadStudentsForProgram(poiId)
        viewModel.loadTasksForPoi(poiId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        ExposedDropdownMenuBox(
            expanded = expandedInstructor,
            onExpandedChange = { expandedInstructor = !expandedInstructor }
        ) {
            TextField(
                readOnly = true,
                value = selectedInstructorName,
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
                // Load instructors from DemographicsViewModel if integrated, or placeholder
                DropdownMenuItem(
                    text = { Text("Instructor 1") },
                    onClick = { selectedInstructorName = "Instructor 1"; expandedInstructor = false }
                )
                // Add more instructors
            }
        }

        when (val state = studentsState) {
            is AppState.Loading -> Text("Loading students...")
            is AppState.Success -> {
                LazyRow {
                    item {
                        Text("Tasks / Students", modifier = Modifier.padding(end = 8.dp))
                    }
                    items(tasksState.let { if (it is AppState.Success) it.data else emptyList() }) { task ->
                        Text(task.name, modifier = Modifier.padding(end = 8.dp))
                    }
                }
                LazyColumn {
                    items(state.data) { student ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(student.fullName, modifier = Modifier.weight(1f))
                            LazyRow {
                                items(tasksState.let { if (it is AppState.Success) it.data else emptyList() }) { task ->
                                    var average by remember { mutableStateOf(0.0) }
                                    LaunchedEffect(student.id, task.id) {
                                        average = viewModel.getAverageScoreForStudent(student.id, task.id) // Update to per-task average
                                    }
                                    Button(
                                        onClick = { selectedStudent = student; selectedTask = task; /* Navigate to GradingScreen */ navController.navigate("grading/${student.id}/${task.id}") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("$average%")
                                    }
                                }
                            }
                            Button(
                                onClick = { selectedStudent = student; showComments = true; viewModel.loadCommentsForStudent(student.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Comments")
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error loading students: ${state.message}")
        }

        if (showComments) {
            AlertDialog(
                onDismissRequest = { showComments = false },
                title = { Text("Comments for ${selectedStudent?.fullName}") },
                text = {
                    when (val state = commentsState) {
                        is AppState.Loading -> Text("Loading comments...")
                        is AppState.Success -> state.data.forEach { comment ->
                            Text(comment)
                        }
                        is AppState.Error -> Text("Error: ${state.message}")
                    }
                },
                confirmButton = {
                    Button(onClick = { showComments = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}