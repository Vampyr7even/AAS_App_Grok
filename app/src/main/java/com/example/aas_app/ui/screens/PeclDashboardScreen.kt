package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.PeclViewModel
import com.example.aas_app.viewmodel.AppState

@Composable
fun PeclDashboardScreen(navController: NavController, poiId: Long) {
    val viewModel: PeclViewModel = hiltViewModel()
    val studentsState by viewModel.studentsState.observeAsState(AppState.Loading as AppState<List<UserEntity>>)
    val resultsState by viewModel.evaluationResultsState.observeAsState(AppState.Loading as AppState<List<PeclEvaluationResultEntity>>)
    val commentsState by viewModel.commentsState.observeAsState(AppState.Loading as AppState<List<String>>)

    var selectedStudent by remember { mutableStateOf<UserEntity?>(null) }
    var showComments by remember { mutableStateOf(false) }

    LaunchedEffect(poiId) {
        viewModel.loadStudentsForProgram(poiId) // Assume added to ViewModel
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = studentsState) {
            is AppState.Loading -> Text("Loading students...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { student ->
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(student.fullName, modifier = Modifier.weight(1f))
                            Text("Progress: ${viewModel.getAverageScoreForStudent(student.id, poiId)}%") // From state
                            Button(
                                onClick = { selectedStudent = student; viewModel.loadEvaluationResultsForStudent(student.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("Grade")
                            }
                            Button(
                                onClick = { selectedStudent = student; showComments = true; viewModel.loadCommentsForStudent(student.id) }, // Assume added
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