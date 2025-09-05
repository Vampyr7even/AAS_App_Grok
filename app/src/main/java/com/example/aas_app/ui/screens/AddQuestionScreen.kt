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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
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
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading)
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading)
    var subTask by remember { mutableStateOf("") }
    var controlType by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var criticalTask by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var expandedTask by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadQuestionsForTask(taskId)
        viewModel.loadTasksForPoi(0L)
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
            label = { Text("Control Type (e.g., ScoreBox, ComboBox, TextBox)") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = scale,
            onValueChange = { scale = it },
            label = { Text("Scale (e.g., Scale_PECL, Scale_Yes_No)") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = criticalTask,
            onValueChange = { criticalTask = it },
            label = { Text("Critical Task (YES/NO)") },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expandedTask,
            onExpandedChange = { expandedTask = !expandedTask }
        ) {
            TextField(
                readOnly = true,
                value = selectedTask?.name ?: "",
                onValueChange = { },
                label = { Text("Select Task") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTask) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedTask,
                onDismissRequest = { expandedTask = false }
            ) {
                when (val state = tasksState) {
                    is AppState.Loading -> DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                    is AppState.Success -> state.data.forEach { task ->
                        DropdownMenuItem(
                            text = { Text(task.name) },
                            onClick = {
                                selectedTask = task
                                expandedTask = false
                            }
                        )
                    }
                    is AppState.Error -> DropdownMenuItem(text = { Text("Error: ${state.message}") }, onClick = {})
                }
            }
        }

        Button(
            onClick = {
                if (subTask.isNotBlank() && controlType.isNotBlank() && scale.isNotBlank() && criticalTask.isNotBlank() && selectedTask != null) {
                    val question = PeclQuestionEntity(
                        id = editingQuestion?.id ?: 0L,
                        subTask = subTask,
                        controlType = controlType,
                        scale = scale,
                        criticalTask = criticalTask
                    )
                    coroutineScope.launch {
                        try {
                            if (editingQuestion == null) {
                                viewModel.insertQuestion(question, selectedTask!!.id)
                                snackbarHostState.showSnackbar("Question added successfully")
                            } else {
                                viewModel.updateQuestion(question, selectedTask!!.id)
                                snackbarHostState.showSnackbar("Question updated successfully")
                            }
                            subTask = ""
                            controlType = ""
                            scale = ""
                            criticalTask = ""
                            selectedTask = null
                            editingQuestion = null
                        } catch (e: Exception) {
                            Log.e("AddQuestionScreen", "Error adding/updating question: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("All fields are required")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (editingQuestion == null) "Add Sub-Task" else "Update Sub-Task")
        }

        when (val state = questionsState) {
            is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { questionWithTask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = questionWithTask.question.subTask,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                IconButton(onClick = {
                                    subTask = questionWithTask.question.subTask
                                    controlType = questionWithTask.question.controlType
                                    scale = questionWithTask.question.scale
                                    criticalTask = questionWithTask.question.criticalTask
                                    editingQuestion = questionWithTask.question
                                    coroutineScope.launch {
                                        val task = viewModel.getTaskById(taskId)
                                        selectedTask = task
                                    }
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Sub-Task")
                                }
                                IconButton(onClick = {
                                    showDeleteDialog = questionWithTask.question
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Sub-Task")
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

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Sub-Task") },
                text = { Text("Are you sure you want to delete ${showDeleteDialog?.subTask}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    showDeleteDialog?.let { viewModel.deleteQuestion(it) }
                                    snackbarHostState.showSnackbar("Question deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("AddQuestionScreen", "Error deleting question: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting question: ${e.message}")
                                }
                            }
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}