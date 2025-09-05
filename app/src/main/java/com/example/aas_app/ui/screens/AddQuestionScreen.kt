package com.example.aas_app.ui.screens

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.QuestionWithTask
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionScreen(navController: NavController, taskId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading)
    var subTask by remember { mutableStateOf("") }
    var controlType by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var criticalTask by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = subTask,
            onValueChange = { subTask = it },
            label = { Text("Subtask Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = controlType,
            onValueChange = { controlType = it },
            label = { Text("Control Type") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = scale,
            onValueChange = { scale = it },
            label = { Text("Scale") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = criticalTask,
            onValueChange = { criticalTask = it },
            label = { Text("Critical Task") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (subTask.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            viewModel.insertQuestion(
                                PeclQuestionEntity(
                                    subTask = subTask,
                                    controlType = controlType,
                                    scale = scale,
                                    criticalTask = criticalTask
                                ),
                                taskId
                            )
                            subTask = ""
                            controlType = ""
                            scale = ""
                            criticalTask = ""
                            snackbarHostState.showSnackbar("Question added successfully")
                        } catch (e: Exception) {
                            Log.e("AddQuestionScreen", "Error adding question: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error adding question: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Subtask name cannot be blank")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Question")
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (questionsState) {
            is AppState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is AppState.Success<*> -> {
                val questions = (questionsState as AppState.Success<List<QuestionWithTask>>).data
                if (questions.isEmpty()) {
                    Text(
                        text = "No questions available.",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn {
                        items(questions) { questionWithTask: QuestionWithTask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = questionWithTask.question.subTask)
                                Row {
                                    IconButton(onClick = {
                                        navController.navigate("editQuestion/${questionWithTask.question.id}/$taskId")
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Question")
                                    }
                                    IconButton(onClick = {
                                        coroutineScope.launch {
                                            try {
                                                viewModel.deleteQuestion(questionWithTask.question)
                                                snackbarHostState.showSnackbar("Question deleted successfully")
                                            } catch (e: Exception) {
                                                Log.e("AddQuestionScreen", "Error deleting question: ${e.message}", e)
                                                snackbarHostState.showSnackbar("Error deleting question: ${e.message}")
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Question")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is AppState.Error -> {
                Text(
                    text = "Error: ${(questionsState as AppState.Error).message}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}