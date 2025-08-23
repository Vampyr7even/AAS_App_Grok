package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState

@Composable
fun RepositoryScreen(navController: NavController) {
    val viewModel: AdminViewModel = hiltViewModel()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading as AppState<List<PeclQuestionEntity>>)

    LaunchedEffect(Unit) {
        viewModel.loadAllQuestions()
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf<PeclQuestionEntity?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = questionsState) {
            is AppState.Loading -> Text("Loading...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { question ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(question.subTask)
                            IconButton(onClick = { /* Edit logic */ }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedQuestion = question; showDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        Button(
            onClick = {
                viewModel.insertQuestion(PeclQuestionEntity(task_id = 0L, subTask = "New Question", controlType = "Text", scale = "Scale", criticalTask = "No"), 0L)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add Question")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this question?") },
            confirmButton = {
                Button(onClick = {
                    selectedQuestion?.let { viewModel.deleteQuestion(it) }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}