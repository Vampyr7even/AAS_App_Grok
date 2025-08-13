package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.PeclViewModel
import com.example.aas_app.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluateScreen(navController: NavController) {
    val viewModel: PeclViewModel = hiltViewModel()
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading as AppState<List<PeclTaskEntity>>)
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading as AppState<List<PeclQuestionEntity>>)

    var selectedTask by remember { mutableStateOf<PeclTaskEntity?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = tasksState) {
            is AppState.Loading -> Text("Loading tasks...")
            is AppState.Success -> state.data.forEach { task ->
                Button(onClick = { selectedTask = task; viewModel.loadQuestionsForTask(task.id) }) { // Assume loadQuestionsForTask added
                    Text(task.name)
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        selectedTask?.let { task ->
            when (val state = questionsState) {
                is AppState.Loading -> Text("Loading questions...")
                is AppState.Success -> {
                    // Display questions and grading UI
                }
                is AppState.Error -> Text("Error: ${state.message}")
            }
        }
    }
}