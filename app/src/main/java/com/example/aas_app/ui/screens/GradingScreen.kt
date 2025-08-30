package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
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
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var scoreInputs by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var comment by remember { mutableStateOf("") }

    // Handle questions error state
    LaunchedEffect(questionsState) {
        if (questionsState is AppState.Error) {
            snackbarHostState.showSnackbar(
                "Error loading questions: ${(questionsState as AppState.Error).message}"
            )
        }
    }

    LaunchedEffect(taskId) {
        viewModel.loadQuestionsForTask(taskId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Grading Student (ID: $studentId) for Task (ID: $taskId)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = questionsState) {
            is AppState.Loading -> CircularProgressIndicator()
            is AppState.Success -> {
                if (state.data.isEmpty()) {
                    Text("No questions available for this task.")
                } else {
                    LazyColumn {
                        items(state.data) { question ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = question.subTask,
                                    modifier = Modifier.weight(1f)
                                )
                                TextField(
                                    value = scoreInputs[question.id] ?: "",
                                    onValueChange = { newScore ->
                                        scoreInputs = scoreInputs + (question.id to newScore)
                                    },
                                    label = { Text("Score") },
                                    modifier = Modifier.width(100.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Comment") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                // TODO: Implement save logic using PeclViewModel.insertEvaluationResult
                                snackbarHostState.showSnackbar("Save functionality to be implemented")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
            is AppState.Error -> {
                Text("Error: ${state.message}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.popBackStack() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Back")
        }

        // Snackbar for errors
        SnackbarHost(hostState = snackbarHostState)
    }
}