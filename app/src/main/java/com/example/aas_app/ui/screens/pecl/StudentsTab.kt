package com.example.aas_app.ui.screens.pecl

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@Composable
fun StudentsTab(
    navController: NavController,
    instructorId: Long,
    programId: Long = 0L // Default to 0L for no program filter
) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val studentsState by viewModel.studentsForInstructorAndProgramState.observeAsState(AppState.Success(emptyList()))
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Log.d("StudentsTab", "Loading students for instructorId=$instructorId, programId=$programId")
        try {
            if (programId == 0L) {
                viewModel.loadStudentsForInstructor(instructorId) // Load all students if no programId
            } else {
                viewModel.loadStudentsForInstructorAndProgram(instructorId, programId)
            }
        } catch (e: Exception) {
            Log.e("StudentsTab", "Error loading students: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading students: ${e.message}")
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
        Text(
            text = "Students",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = studentsState) {
            is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is AppState.Success -> {
                if (state.data.isEmpty()) {
                    Text(text = "No students assigned.")
                } else {
                    LazyColumn {
                        items(state.data) { student ->
                            var programName by remember { mutableStateOf("Loading...") }
                            LaunchedEffect(programId) {
                                if (programId != 0L) {
                                    try {
                                        val program: PeclProgramEntity? = viewModel.getProgramById(programId)
                                        programName = program?.name ?: "Unknown Program"
                                    } catch (e: Exception) {
                                        Log.e("StudentsTab", "Error loading program: ${e.message}", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Error loading program: ${e.message}")
                                        }
                                    }
                                } else {
                                    programName = "All Programs"
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = student.fullName)
                                Row {
                                    IconButton(onClick = {
                                        try {
                                            Log.d("StudentsTab", "Navigating to editStudent/${student.id}")
                                            navController.navigate("editStudent/${student.id}")
                                        } catch (e: Exception) {
                                            Log.e("StudentsTab", "Navigation error to editStudent/${student.id}: ${e.message}", e)
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Student")
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            try {
                                                viewModel.deletePeclStudent(student)
                                                Log.d("StudentsTab", "Deleted student: ${student.fullName}")
                                                snackbarHostState.showSnackbar("Student deleted successfully")
                                            } catch (e: Exception) {
                                                Log.e("StudentsTab", "Error deleting student: ${e.message}", e)
                                                snackbarHostState.showSnackbar("Error deleting student: ${e.message}")
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Student")
                                    }
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

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                try {
                    Log.d("StudentsTab", "Navigating to addStudent")
                    navController.navigate("addStudent")
                } catch (e: Exception) {
                    Log.e("StudentsTab", "Navigation error to addStudent: ${e.message}", e)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add Student")
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}