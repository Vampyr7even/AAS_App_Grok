package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.QuestionWithTask
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionScreen(
    navController: NavController,
    taskId: Long,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(State.Success(emptyList()))
    val tasksState by viewModel.tasksState.observeAsState(State.Success(emptyList()))
    val taskState by viewModel.taskState.observeAsState(State.Success(null))
    var subTask by remember { mutableStateOf("") }
    var controlType by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var criticalTask by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var expandedTask by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(taskId) {
        viewModel.loadQuestionsForTask(taskId)
        viewModel.loadTasksForPoi(0L)
        viewModel.getTaskById(taskId)
    }

    LaunchedEffect(taskState) {
        when (val state = taskState) {
            is State.Success -> {
                selectedTask = state.data
            }
            is State.Error -> {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading task: ${state.message}")
                }
            }
            is State.Loading -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Manage Sub-Tasks", style = MaterialTheme.typography.headlineSmall)

        TextField(
            value = subTask,
            onValueChange = { subTask = it },
            label = { Text("Sub-Task Name") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = controlType,
            onValueChange = { controlType = it },
            label = { Text("Control Type") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = scale,
            onValueChange = { scale = it },
            label = { Text("Scale") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = criticalTask,
            onValueChange = { criticalTask = it },
            label = { Text("Critical Task") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (subTask.isNotBlank() && controlType.isNotBlank() && scale.isNotBlank()) {
                        coroutineScope.launch {
                            try {
                                val question = PeclQuestionEntity(
                                    subTask = subTask,
                                    controlType = controlType,
                                    scale = scale,
                                    criticalTask = criticalTask
                                )
                                if (editingQuestion != null) {
                                    viewModel.updateQuestion(question.copy(id = editingQuestion!!.id), taskId)
                                    Toast.makeText(context, "Question updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.insertQuestion(question, taskId)
                                    Toast.makeText(context, "Question added successfully", Toast.LENGTH_SHORT).show()
                                }
                                subTask = ""
                                controlType = ""
                                scale = ""
                                criticalTask = ""
                                editingQuestion = null
                            } catch (e: Exception) {
                                Log.e("AddQuestionScreen", "Error saving question: ${e.message}", e)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error saving question: ${e.message}")
                                }
                            }
                        }
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("All fields except Critical Task must be filled")
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(if (editingQuestion != null) "Update" else "Save")
            }
            Button(
                onClick = {
                    subTask = ""
                    controlType = ""
                    scale = ""
                    criticalTask = ""
                    editingQuestion = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("Clear")
            }
        }

        when (val state = questionsState) {
            is State.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is State.Success -> {
                if (state.data.isEmpty()) {
                    Text("No questions available")
                } else {
                    LazyColumn {
                        items(state.data) { questionWithTask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = questionWithTask.question.subTask)
                                    Text(
                                        text = "Task: ${questionWithTask.task?.name ?: "None"}, ControlType: ${questionWithTask.question.controlType}, Scale: ${questionWithTask.question.scale}, CriticalTask: ${questionWithTask.question.criticalTask}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = {
                                    editingQuestion = questionWithTask.question
                                    subTask = questionWithTask.question.subTask
                                    controlType = questionWithTask.question.controlType
                                    scale = questionWithTask.question.scale
                                    criticalTask = questionWithTask.question.criticalTask
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { showDeleteDialog = questionWithTask.question }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
            is State.Error -> Text(
                text = "Error: ${state.message}",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete this question?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    showDeleteDialog?.let { viewModel.deleteQuestion(it) }
                                    showDeleteDialog = null
                                    Toast.makeText(context, "Question deleted successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("AddQuestionScreen", "Error deleting question: ${e.message}", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error deleting question: ${e.message}")
                                    }
                                }
                            }
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

        SnackbarHost(hostState = snackbarHostState)
    }
}