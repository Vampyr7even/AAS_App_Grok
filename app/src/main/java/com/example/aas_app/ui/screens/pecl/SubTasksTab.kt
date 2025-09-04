package com.example.aas_app.ui.screens.pecl

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.TaskWithPois
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.QuestionWithTask
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubTasksTab(adminViewModel: AdminViewModel, errorMessage: String?, snackbarHostState: SnackbarHostState, coroutineScope: CoroutineScope) {
    val context = LocalContext.current
    val questionsWithTasksState by adminViewModel.questionsState.observeAsState(AppState.Loading)
    val scalesState by adminViewModel.scalesState.observeAsState(AppState.Loading)
    val tasksState by adminViewModel.tasksState.observeAsState(AppState.Loading)
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var newSubTask by remember { mutableStateOf("") }
    var newControlType by remember { mutableStateOf("") }
    var newScale by remember { mutableStateOf("") }
    var newCriticalTask by remember { mutable stateOf("") }
    var newTaskId by remember { mutableStateOf<Long?>(null) }
    var showEditQuestionDialog by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var editSubTask by remember { mutableStateOf("") }
    var editControlType by remember { mutableStateOf("") }
    var editScale by remember { mutableStateOf("") }
    var editCriticalTask by remember { mutableStateOf("") }
    var editTaskId by remember { mutableStateOf<Long?>(null) }
    var selectedQuestionToDelete by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var expandedScaleAdd by remember { mutableStateOf(false) }
    var expandedTaskAdd by remember { mutableStateOf(false) }
    var expandedControlTypeAdd by remember { mutableStateOf(false) }
    var expandedCriticalTaskAdd by remember { mutableStateOf(false) }
    var expandedScaleEdit by remember { mutableStateOf(false) }
    var expandedTaskEdit by remember { mutableStateOf(false) }
    var expandedControlTypeEdit by remember { mutableStateOf(false) }
    var expandedCriticalTaskEdit by remember { mutable stateOf(false) }
    val controlTypeOptions = listOf("CheckBox", "ComboBox", "Comment", "ListBox", "OptionButton", "ScoreBox", "TextBox")
    val criticalTaskOptions = listOf("No", "Yes")

    LaunchedEffect(Unit) {
        Log.d("SubTasksTab", "Loading questions, scales, and tasks")
        try {
            adminViewModel.loadQuestions()
            adminViewModel.loadScales()
            adminViewModel.loadTasks()
        } catch (e: Exception) {
            Log.e("SubTasksTab", "Error loading subtasks: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading subtasks: ${e.message}")
            }
        }
    }

    LaunchedEffect(showEditQuestionDialog) {
        showEditQuestionDialog?.let { question: PeclQuestionEntity ->
            editSubTask = question.subTask
            editControlType = question.controlType
            editScale = question.scale
            editCriticalTask = question.criticalTask
            editTaskId = null // Since task_id removed, select new task
        }
    }

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
        IconButton(onClick = { showAddQuestionDialog = true }) {
            Icon(Icons.Filled.Add, contentDescription = "Add Question")
        }
        Text("Add Question", modifier = Modifier.padding(start = 4.dp))
    }

    when (val state = questionsWithTasksState) {
        is AppState.Loading -> Text("Loading...")
        is AppState.Success -> {
            val sortedQuestions = state.data.sortedBy { it.question.subTask }
            if (sortedQuestions.isEmpty()) {
                Text("No questions available")
            } else {
                LazyColumn {
                    items(sortedQuestions) { questionWithTask ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = questionWithTask.question.subTask)
                                Text(text = "Task: ${questionWithTask.task?.name ?: "None"}, ControlType: ${questionWithTask.question.controlType}, Scale: ${questionWithTask.question.scale}, CriticalTask: ${questionWithTask.question.criticalTask}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { showEditQuestionDialog = questionWithTask.question }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedQuestionToDelete = questionWithTask.question }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
        is AppState.Error -> Text("Error: ${state.message}")
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
                    ExposedDropdownMenuBox(
                        expanded = expandedControlTypeAdd,
                        onExpandedChange = { expandedControlTypeAdd = !expandedControlTypeAdd }
                    ) {
                        TextField(
                            readOnly = true,
                            value = newControlType,
                            onValueChange = { },
                            label = { Text("Control Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedControlTypeAdd) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedControlTypeAdd,
                            onDismissRequest = { expandedControlTypeAdd = false }
                        ) {
                            controlTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        newControlType = option
                                        expandedControlTypeAdd = false
                                    }
                                )
                            }
                        }
                    }
                    if (newControlType == "ComboBox" || newControlType == "ListBox") {
                        ExposedDropdownMenuBox(
                            expanded = expandedScaleAdd,
                            onExpandedChange = { expandedScaleAdd = !expandedScaleAdd }
                        ) {
                            TextField(
                                readOnly = true,
                                value = newScale,
                                onValueChange = { },
                                label = { Text("Scale") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedScaleAdd) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedScaleAdd,
                                onDismissRequest = { expandedScaleAdd = false }
                            ) {
                                when (val state = scalesState) {
                                    is AppState.Loading -> Text("Loading scales...")
                                    is AppState.Success<List<ScaleEntity>> -> state.data.sortedBy { it.scaleName }.forEach { scale ->
                                        DropdownMenuItem(
                                            text = { Text(scale.scaleName) },
                                            onClick = {
                                                newScale = scale.scaleName
                                                expandedScaleAdd = false
                                            }
                                        )
                                    }
                                    is AppState.Error -> Text("Error: ${state.message}")
                                }
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedTaskAdd,
                        onExpandedChange = { expandedTaskAdd = !expandedTaskAdd }
                    ) {
                        TextField(
                            readOnly = true,
                            value = if (tasksState is AppState.Success) {
                                (tasksState as AppState.Success<List<PeclTaskEntity>>).data.sortedBy { it.name }.find { it.id == newTaskId }?.name ?: ""
                            } else "",
                            onValueChange = { },
                            label = { Text("Assign to Task") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTaskAdd) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTaskAdd,
                            onDismissRequest = { expandedTaskAdd = false }
                        ) {
                            when (val state = tasksState) {
                                is AppState.Loading -> Text("Loading tasks...")
                                is AppState.Success<List<PeclTaskEntity>> -> state.data.sortedBy { it.name }.forEach { task ->
                                    DropdownMenuItem(
                                        text = { Text(task.name) },
                                        onClick = {
                                            newTaskId = task.id
                                            expandedTaskAdd = false
                                        }
                                    )
                                }
                                is AppState.Error -> Text("Error: ${state.message}")
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedCriticalTaskAdd,
                        onExpandedChange = { expandedCriticalTaskAdd = !expandedCriticalTaskAdd }
                    ) {
                        TextField(
                            readOnly = true,
                            value = newCriticalTask,
                            onValueChange = { },
                            label = { Text("Critical Task") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCriticalTaskAdd) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCriticalTaskAdd,
                            onDismissRequest = { expandedCriticalTaskAdd = false }
                        ) {
                            criticalTaskOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        newCriticalTask = option
                                        expandedCriticalTaskAdd = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val taskIdValue = newTaskId
                        if (taskIdValue != null) {
                            if (newSubTask.isNotBlank()) {
                                try {
                                    adminViewModel.insertQuestion(PeclQuestionEntity(subTask = newSubTask, controlType = newControlType, scale = newScale, criticalTask = newCriticalTask), taskIdValue)
                                    showAddQuestionDialog = false
                                    newSubTask = ""
                                    newControlType = ""
                                    newScale = ""
                                    newCriticalTask = ""
                                    newTaskId = null
                                    Toast.makeText(context, "Question added successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("SubTasksTab", "Error adding question: ${e.message}", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error adding question: ${e.message}")
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Sub task is required")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Task assignment is required")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = newSubTask.isNotBlank() && newTaskId != null
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

    showEditQuestionDialog?.let { question: PeclQuestionEntity ->
        AlertDialog(
            onDismissRequest = { showEditQuestionDialog = null },
            title = { Text("Edit Question") },
            text = {
                Column {
                    TextField(
                        value = editSubTask,
                        onValueChange = { editSubTask = it },
                        label = { Text("Sub Task") }
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedControlTypeEdit,
                        onExpandedChange = { expandedControlTypeEdit = !expandedControlTypeEdit }
                    ) {
                        TextField(
                            readOnly = true,
                            value = editControlType,
                            onValueChange = { },
                            label = { Text("Control Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedControlTypeEdit) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedControlTypeEdit,
                            onDismissRequest = { expandedControlTypeEdit = false }
                        ) {
                            controlTypeOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        editControlType = option
                                        expandedControlTypeEdit = false
                                    }
                                )
                            }
                        }
                    }
                    if (editControlType == "ComboBox" || editControlType == "ListBox") {
                        ExposedDropdownMenuBox(
                            expanded = expandedScaleEdit,
                            onExpandedChange = { expandedScaleEdit = !expandedScaleEdit }
                        ) {
                            TextField(
                                readOnly = true,
                                value = editScale,
                                onValueChange = { },
                                label = { Text("Scale") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedScaleEdit) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedScaleEdit,
                                onDismissRequest = { expandedScaleEdit = false }
                            ) {
                                when (val state = scalesState) {
                                    is AppState.Loading -> Text("Loading scales...")
                                    is AppState.Success<List<ScaleEntity>> -> state.data.sortedBy { it.scaleName }.forEach { scale ->
                                        DropdownMenuItem(
                                            text = { Text(scale.scaleName) },
                                            onClick = {
                                                editScale = scale.scaleName
                                                expandedScaleEdit = false
                                            }
                                        )
                                    }
                                    is AppState.Error -> Text("Error: ${state.message}")
                                }
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedTaskEdit,
                        onExpandedChange = { expandedTaskEdit = !expandedTaskEdit }
                    ) {
                        TextField(
                            readOnly = true,
                            value = if (tasksState is AppState.Success) {
                                (tasksState as AppState.Success<List<PeclTaskEntity>>).data.sortedBy { it.name }.find { it.id == editTaskId }?.name ?: ""
                            } else "",
                            onValueChange = { },
                            label = { Text("Assign to Task") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTaskEdit) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTaskEdit,
                            onDismissRequest = { expandedTaskEdit = false }
                        ) {
                            when (val state = tasksState) {
                                is AppState.Loading -> Text("Loading tasks...")
                                is AppState.Success<List<PeclTaskEntity>> -> state.data.sortedBy { it.name }.forEach { task ->
                                    DropdownMenuItem(
                                        text = { Text(task.name) },
                                        onClick = {
                                            editTaskId = task.id
                                            expandedTaskEdit = false
                                        }
                                    )
                                }
                                is AppState.Error -> Text("Error: ${state.message}")
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedCriticalTaskEdit,
                        onExpandedChange = { expandedCriticalTaskEdit = !expandedCriticalTaskEdit }
                    ) {
                        TextField(
                            readOnly = true,
                            value = editCriticalTask,
                            onValueChange = { },
                            label = { Text("Critical Task") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCriticalTaskEdit) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCriticalTaskEdit,
                            onDismissRequest = { expandedCriticalTaskEdit = false }
                        ) {
                            criticalTaskOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        editCriticalTask = option
                                        expandedCriticalTaskEdit = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val taskIdValue = editTaskId
                        if (taskIdValue != null) {
                            if (editSubTask.isNotBlank()) {
                                try {
                                    adminViewModel.updateQuestion(question.copy(subTask = editSubTask, controlType = editControlType, scale = editScale, criticalTask = editCriticalTask), taskIdValue)
                                    showEditQuestionDialog = null
                                    editSubTask = ""
                                    editControlType = ""
                                    editScale = ""
                                    editCriticalTask = ""
                                    editTaskId = null
                                    Toast.makeText(context, "Question updated successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("SubTasksTab", "Error updating question: ${e.message}", e)
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error updating question: ${e.message}")
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Sub task is required")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Task assignment is required")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = editSubTask.isNotBlank() && editTaskId != null
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditQuestionDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedQuestionToDelete?.let { question ->
        AlertDialog(
            onDismissRequest = { selectedQuestionToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this question?") },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            adminViewModel.deleteQuestion(question)
                            selectedQuestionToDelete = null
                            Toast.makeText(context, "Question deleted successfully", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("SubTasksTab", "Error deleting question: ${e.message}", e)
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error deleting question: ${e.message}")
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
                    onClick = { selectedQuestionToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("No")
                }
            }
        )
    }
}