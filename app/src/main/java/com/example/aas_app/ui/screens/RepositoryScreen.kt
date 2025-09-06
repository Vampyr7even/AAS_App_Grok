package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.QuestionWithTask
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun RepositoryScreen(
    navController: NavController,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Success(emptyList()))
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var newSubTask by remember { mutableStateOf("") }
    var newControlType by remember { mutableStateOf("") }
    var newScale by remember { mutableStateOf("") }
    var newCriticalTask by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadQuestionsForTask(0L)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question Repository",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddQuestionDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Question")
            }
            Text("Add Question", modifier = Modifier.padding(start = 4.dp))
        }

        when (val state = questionsState) {
            is AppState.Loading -> Text("Loading...")
            is AppState.Success -> {
                if (state.data.isEmpty()) {
                    Text("No questions available")
                } else {
                    LazyColumn {
                        items(state.data) { questionWithTask ->
                            val question = questionWithTask.question
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(question.subTask)
                                    Text(
                                        text = "Task: ${questionWithTask.task?.name ?: "None"}, ControlType: ${question.controlType}, Scale: ${question.scale}, CriticalTask: ${question.criticalTask}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { navController.navigate("editQuestion/${question.id}/${questionWithTask.task?.id ?: 0L}") }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    showDeleteDialog = question
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
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

        if (showAddQuestionDialog) {
            AlertDialog(
                onDismissRequest = { showAddQuestionDialog = false },
                title = { Text("Add Question") },
                text = {
                    Column {
                        TextField(
                            value = newSubTask,
                            onValueChange = { newSubTask = it },
                            label = { Text("Sub Task") }
                        )
                        TextField(
                            value = newControlType,
                            onValueChange = { newControlType = it },
                            label = { Text("Control Type") }
                        )
                        TextField(
                            value = newScale,
                            onValueChange = { newScale = it },
                            label = { Text("Scale") }
                        )
                        TextField(
                            value = newCriticalTask,
                            onValueChange = { newCriticalTask = it },
                            label = { Text("Critical Task") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSubTask.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        viewModel.insertQuestion(
                                            PeclQuestionEntity(
                                                subTask = newSubTask,
                                                controlType = newControlType,
                                                scale = newScale,
                                                criticalTask = newCriticalTask
                                            ),
                                            0L
                                        )
                                        showAddQuestionDialog = false
                                        newSubTask = ""
                                        newControlType = ""
                                        newScale = ""
                                        newCriticalTask = ""
                                        Toast.makeText(context, "Question added successfully", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("RepositoryScreen", "Error adding question: ${e.message}", e)
                                        snackbarHostState.showSnackbar("Error adding question: ${e.message}")
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Sub task is required")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showAddQuestionDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Confirm Delete") },
                text = { Text("Delete this question?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    showDeleteDialog?.let { viewModel.deleteQuestion(it) }
                                    snackbarHostState.showSnackbar("Question deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("RepositoryScreen", "Error deleting question: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting question: ${e.message}")
                                }
                            }
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("No")
                    }
                }
            )
        }
    }
}