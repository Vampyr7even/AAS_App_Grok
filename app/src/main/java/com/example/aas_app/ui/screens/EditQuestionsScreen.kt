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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionsScreen(navController: NavController, taskId: Long) {
    val viewModel: AdminViewModel = hiltViewModel()
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading as AppState<List<PeclQuestionEntity>>)
    val scalesState by viewModel.scalesState.observeAsState(AppState.Loading as AppState<List<ScaleEntity>>)

    LaunchedEffect(taskId) {
        viewModel.loadQuestionsForTask(taskId)
        viewModel.loadScales()
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedQuestion by remember { mutableStateOf<PeclQuestionEntity?>(null) }
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
                            Text(text = question.subTask, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                editQuestion = question
                                editSubTask = question.subTask
                                editControlType = question.controlType
                                editScale = question.scale
                                editCriticalTask = question.criticalTask
                            }) {
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

        TextField(
            value = newSubTask,
            onValueChange = { newSubTask = it },
            label = { Text("New Sub Task") }
        )
        TextField(
            value = newControlType,
            onValueChange = { newControlType = it },
            label = { Text("Control Type") }
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
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                when (val state = scalesState) {
                    is AppState.Loading -> Text("Loading scales...")
                    is AppState.Success -> state.data.forEach { scale ->
                        DropdownMenuItem(
                            text = { Text(scale.scaleName) },
                            onClick = {
                                newScale = scale.scaleName
                                expanded = false
                            }
                        )
                    }
                    is AppState.Error -> Text("Error: ${state.message}")
                }
            }
        }
        TextField(
            value = newCriticalTask,
            onValueChange = { newCriticalTask = it },
            label = { Text("Critical Task") }
        )
        Button(
            onClick = {
                viewModel.insertQuestion(PeclQuestionEntity(subTask = newSubTask, controlType = newControlType, scale = newScale, criticalTask = newCriticalTask), taskId)
                newSubTask = ""
                newControlType = ""
                newScale = ""
                newCriticalTask = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
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
                            label = { Text("Sub Task") }
                        )
                        TextField(
                            value = editControlType,
                            onValueChange = { editControlType = it },
                            label = { Text("Control Type") }
                        )
                        TextField(
                            value = editScale,
                            onValueChange = { editScale = it },
                            label = { Text("Scale") }
                        )
                        TextField(
                            value = editCriticalTask,
                            onValueChange = { editCriticalTask = it },
                            label = { Text("Critical Task") }
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updateQuestion(question.copy(subTask = editSubTask, controlType = editControlType, scale = editScale, criticalTask = editCriticalTask), taskId)
                        editQuestion = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { editQuestion = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this question?") },
            confirmButton = {
                Button(onClick = {
                    selectedQuestion?.let { viewModel.deleteQuestion(it, taskId) }
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