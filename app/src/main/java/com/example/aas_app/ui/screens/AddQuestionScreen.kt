package com.example.aas_app.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun AddQuestionScreen(navController: NavController) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading)
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var subTask by remember { mutableStateOf("") }
    var controlType by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var criticalTask by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var expandedTask by remember { mutableStateOf(false) }
    var editingQuestion by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PeclQuestionEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAllQuestions()
        viewModel.loadAllTasks()
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
            modifier = Modifier.fillMaxSize()
        )

        TextField(
            value = controlType,
            onValueChange = { controlType = it },
            label = { Text("Control Type (e.g., ScoreBox, ComboBox, TextBox)") },
            modifier = Modifier.fillMaxSize()
        )

        TextField(
            value = scale,
            onValueChange = { scale = it },
            label = { Text("Scale (e.g., Scale_PECL, Scale_Yes_No)") },
            modifier = Modifier.fillMaxSize()
        )

        TextField(
            value = criticalTask,
            onValueChange = { criticalTask = it },
            label = { Text("Critical Task (YES/NO)") },
            modifier = Modifier.fillMaxSize()
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
                    .fillMaxSize()
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
                        if (editingQuestion == null) {
                            viewModel.insertQuestion(question, selectedTask!!.id)
                        } else {
                            viewModel.updateQuestion(question, selectedTask!!.id)
                        }
                        // Reset form
                        subTask = ""
                        controlType = ""
                        scale = ""
                        criticalTask = ""
                        selectedTask = null
                        editingQuestion = null
                        Toast.makeText(context, if (editingQuestion == null) "Added" else "Updated", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("All fields required")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(if (editingQuestion == null) "Add Sub-Task" else "Update Sub-Task")
        }

        when (questionsState) {
            is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is AppState.Success -> {
                LazyColumn {
                    items(questionsState.data) { question ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = question.subTask,
                                modifier = Modifier.weight(1f)
                            )
                            Row {
                                IconButton(onClick = {
                                    editingQuestion = question
                                    subTask = question.subTask
                                    controlType = question.controlType
                                    scale = question.scale
                                    criticalTask = question.criticalTask
                                    viewModel.getTaskForQuestion(question.id) { task ->
                                        selectedTask = task
                                    }
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Sub-Task")
                                }
                                IconButton(onClick = {
                                    showDeleteDialog = question
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Sub-Task")
                                }
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text(
                text = "Error: ${questionsState.message}",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize()
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
                                showDeleteDialog?.let { viewModel.deleteQuestion(it) }
                                showDeleteDialog = null
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                            }
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