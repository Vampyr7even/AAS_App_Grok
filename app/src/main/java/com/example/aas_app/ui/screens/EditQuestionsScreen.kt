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
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionsScreen(
    navController: NavController,
    taskId: Long,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(State.Success(emptyList()))
    val scalesState by viewModel.scalesState.observeAsState(State.Success(emptyList()))
    var showDeleteDialog by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var editQuestion by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var editSubTask by remember { mutableStateOf("") }
    var editControlType by remember { mutableStateOf("") }
    var editScale by remember { mutableStateOf("") }
    var editCriticalTask by remember { mutableStateOf("") }
    var newSubTask by remember { mutableStateOf("") }
    var newControlType by remember { mutableStateOf("") }
    var newScale by remember { mutableStateOf("") }
    var newCriticalTask by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadQuestionsForTask(taskId)
        viewModel.loadScales()
    }

    LaunchedEffect(editQuestion) {
        editQuestion?.let { question ->
            editSubTask = question.subTask
            editControlType = question.controlType
            editScale = question.scale
            editCriticalTask = question.criticalTask
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Questions",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sub Tasks - Questions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { navController.navigate("addQuestion/$taskId") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Question")
            }
            Text("Add Question", modifier = Modifier.padding(start = 4.dp))
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
                                    editQuestion = questionWithTask.question
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

        TextField(
            value = newSubTask,
            onValueChange = { newSubTask = it },
            label = { Text("New Sub Task") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = newControlType,
            onValueChange = { newControlType = it },
            label = { Text("Control Type") },
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = newScale,
                onValueChange = { },
                label = { Text("Scale") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                when (val state = scalesState) {
                    is State.Loading -> {
                        DropdownMenuItem(
                            text = { Text("Loading scales...") },
                            onClick = {}
                        )
                    }
                    is State.Success -> {
                        state.data.sortedBy { it.scaleName }.forEach { scale ->
                            DropdownMenuItem(
                                text = { Text(scale.scaleName) },
                                onClick = {
                                    newScale = scale.scaleName
                                    expanded = false
                                }
                            )
                        }
                    }
                    is State.Error -> {
                        DropdownMenuItem(
                            text = { Text("Error: ${state.message}") },
                            onClick = {}
                        )
                    }
                }
            }
        }
        TextField(
            value = newCriticalTask,
            onValueChange = { newCriticalTask = it },
            label = { Text("Critical Task") },
            modifier = Modifier.fillMaxWidth()
        )
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
                                taskId
                            )
                            newSubTask = ""
                            newControlType = ""
                            newScale = ""
                            newCriticalTask = ""
                            Toast.makeText(context, "Question added successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("EditQuestionsScreen", "Error adding question: ${e.message}", e)
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
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Question")
        }

        editQuestion?.let { question ->
            AlertDialog(
                onDismissRequest = { editQuestion = null },
                title = { Text("Edit Question") },
                text = {
                    Column {
                        TextField(
                            value = editSubTask,
                            onValueChange = { editSubTask = it },
                            label = { Text("Sub Task") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = editControlType,
                            onValueChange = { editControlType = it },
                            label = { Text("Control Type") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = editScale,
                            onValueChange = { editScale = it },
                            label = { Text("Scale") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = editCriticalTask,
                            onValueChange = { editCriticalTask = it },
                            label = { Text("Critical Task") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    viewModel.updateQuestion(
                                        question.copy(
                                            subTask = editSubTask,
                                            controlType = editControlType,
                                            scale = editScale,
                                            criticalTask = editCriticalTask
                                        ),
                                        taskId
                                    )
                                    editQuestion = null
                                    Toast.makeText(context, "Question updated successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("EditQuestionsScreen", "Error updating question: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error updating question: ${e.message}")
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
                        onClick = { editQuestion = null },
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
                                    Log.e("EditQuestionsScreen", "Error deleting question: ${e.message}", e)
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

        SnackbarHost(hostState = snackbarHostState)
    }
}