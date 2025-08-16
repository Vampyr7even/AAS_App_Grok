package com.example.aas_app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PoiWithPrograms
import com.example.aas_app.viewmodel.TaskWithPois

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeclScreen(navController: NavController) {
    val viewModel: AdminViewModel = hiltViewModel()
    val programsState by viewModel.programsState.observeAsState(AppState.Loading as AppState<List<PeclProgramEntity>>)
    val poisState by viewModel.poisState.observeAsState(AppState.Loading as AppState<List<PoiWithPrograms>>)
    val tasksState by viewModel.tasksState.observeAsState(AppState.Loading as AppState<List<TaskWithPois>>)
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading as AppState<List<PeclQuestionEntity>>)
    val scalesState by viewModel.scalesState.observeAsState(AppState.Loading as AppState<List<ScaleEntity>>)
    val poisSimple by viewModel.poisSimple.observeAsState(emptyList())
    var selectedTab by remember { mutableStateOf<String?>(null) }
    var showAddProgram by remember { mutableStateOf(false) }
    var newProgramName by remember { mutableStateOf("") }
    var selectedProgramToDelete by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var showAddPoiDialog by remember { mutableStateOf(false) }
    var newPoiName by remember { mutableStateOf("") }
    var selectedProgramsForAdd by remember { mutableStateOf(setOf<Long>()) }
    var showEditPoiDialog by remember { mutableStateOf<PoiWithPrograms?>(null) }
    var editPoiName by remember { mutableStateOf("") }
    var selectedProgramsForEdit by remember { mutableStateOf(setOf<Long>()) }
    var selectedPoiToDelete by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskName by remember { mutableStateOf("") }
    var selectedPoisForAdd by remember { mutableStateOf(setOf<Long>()) }
    var showEditTaskDialog by remember { mutableStateOf<TaskWithPois?>(null) }
    var editTaskName by remember { mutableStateOf("") }
    var selectedPoisForEdit by remember { mutableStateOf(setOf<Long>()) }
    var selectedTaskToDelete by remember { mutableStateOf<PeclTaskEntity?>(null) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var newSubTask by remember { mutableStateOf("") }
    var newControlType by remember { mutableStateOf("") }
    var newScale by remember { mutableStateOf("") }
    var newCriticalTask by remember { mutableStateOf("") }
    var showEditQuestionDialog by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var editSubTask by remember { mutableStateOf("") }
    var editControlType by remember { mutableStateOf("") }
    var editScale by remember { mutableStateOf("") }
    var editCriticalTask by remember { mutableStateOf("") }
    var selectedQuestionToDelete by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Programs") {
            viewModel.loadPrograms()
        } else if (selectedTab == "POI") {
            viewModel.loadPrograms() // For add/edit dialogs
            viewModel.loadAllPoisWithPrograms()
        } else if (selectedTab == "Tasks") {
            viewModel.loadAllPois() // For selection in dialogs
            viewModel.loadAllTasksWithPois()
        } else if (selectedTab == "Sub Tasks") {
            viewModel.loadAllQuestions()
            viewModel.loadScales()
        }
    }

    LaunchedEffect(showEditPoiDialog) {
        showEditPoiDialog?.let { poiWithPrograms ->
            editPoiName = poiWithPrograms.poi.name
            selectedProgramsForEdit = poiWithPrograms.programs.mapNotNull { programName ->
                programsState.let { state ->
                    if (state is AppState.Success) {
                        state.data.find { it.name == programName }?.id
                    } else null
                }
            }.toSet()
        }
    }

    LaunchedEffect(showEditTaskDialog) {
        showEditTaskDialog?.let { taskWithPois ->
            editTaskName = taskWithPois.task.name
            selectedPoisForEdit = taskWithPois.pois.mapNotNull { poiName ->
                poisSimple.find { it.name == poiName }?.id
            }.toSet()
        }
    }

    LaunchedEffect(showEditQuestionDialog) {
        showEditQuestionDialog?.let { question ->
            editSubTask = question.subTask
            editControlType = question.controlType
            editScale = question.scale
            editCriticalTask = question.criticalTask
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Performance Evaluation Checklist",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            PeclModuleNavButton("Evaluate", selectedTab == "Evaluate") { navController.navigate("pecl/evaluate"); selectedTab = "Evaluate" }
            PeclModuleNavButton("Programs", selectedTab == "Programs") { selectedTab = "Programs" }
            PeclModuleNavButton("POI", selectedTab == "POI") { selectedTab = "POI" }
            PeclModuleNavButton("Tasks", selectedTab == "Tasks") { selectedTab = "Tasks" }
            PeclModuleNavButton("Sub Tasks", selectedTab == "Sub Tasks") { selectedTab = "Sub Tasks" }
            PeclModuleNavButton("Instructors", selectedTab == "Instructors") { navController.navigate("pecl/instructors"); selectedTab = "Instructors" }
            PeclModuleNavButton("Students", selectedTab == "Students") { navController.navigate("pecl/students"); selectedTab = "Students" }
        }

        if (selectedTab == "Programs") {
            Text(
                text = "Programs",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            when (val state = programsState) {
                is AppState.Loading -> Text("Loading...")
                is AppState.Success -> {
                    LazyColumn {
                        items(state.data) { program ->
                            var isEditing by remember { mutableStateOf(false) }
                            var editName by remember { mutableStateOf(program.name) }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isEditing) {
                                    TextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        viewModel.updateProgram(program.copy(name = editName))
                                        isEditing = false
                                    }) {
                                        Icon(imageVector = Icons.Filled.Save, contentDescription = "Save")
                                    }
                                    IconButton(onClick = {
                                        editName = program.name
                                        isEditing = false
                                    }) {
                                        Icon(imageVector = Icons.Filled.Cancel, contentDescription = "Cancel")
                                    }
                                } else {
                                    Text(program.name, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        isEditing = true
                                        editName = program.name
                                    }) {
                                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { selectedProgramToDelete = program }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text("Error: ${state.message}")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAddProgram = !showAddProgram }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Program")
                }
                Text("Add Program", modifier = Modifier.padding(start = 4.dp))
            }

            if (showAddProgram) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newProgramName,
                        onValueChange = { newProgramName = it },
                        label = { Text("New Program Name") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        viewModel.insertProgram(PeclProgramEntity(0L, newProgramName))
                        newProgramName = ""
                        showAddProgram = false
                    }) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = "Save New Program")
                    }
                }
            }
        } else if (selectedTab == "POI") {
            Text(
                text = "Program of Instruction",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (val state = poisState) {
                is AppState.Loading -> Text("Loading POIs...")
                is AppState.Success -> {
                    LazyColumn {
                        items(state.data) { poiWithPrograms ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(poiWithPrograms.poi.name)
                                    Text("Programs: ${poiWithPrograms.programs.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { showEditPoiDialog = poiWithPrograms }) {
                                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { selectedPoiToDelete = poiWithPrograms.poi }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text("Error: ${state.message}")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAddPoiDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add POI")
                }
                Text("Add POI", modifier = Modifier.padding(start = 4.dp))
            }

            if (showAddPoiDialog) {
                AlertDialog(
                    onDismissRequest = { showAddPoiDialog = false },
                    title = { Text("Add POI") },
                    text = {
                        Column {
                            TextField(
                                value = newPoiName,
                                onValueChange = { newPoiName = it },
                                label = { Text("POI Name") }
                            )
                            Text("Select Programs:")
                            LazyColumn {
                                items(programsState.let { state ->
                                    if (state is AppState.Success) state.data else emptyList()
                                }) { program ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedProgramsForAdd.contains(program.id),
                                            onCheckedChange = { checked ->
                                                selectedProgramsForAdd = if (checked) {
                                                    selectedProgramsForAdd + program.id
                                                } else {
                                                    selectedProgramsForAdd - program.id
                                                }
                                            }
                                        )
                                        Text(program.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newPoiName.isNotBlank() && selectedProgramsForAdd.isNotEmpty()) {
                                    viewModel.insertPoi(PeclPoiEntity(name = newPoiName), selectedProgramsForAdd.toList())
                                    showAddPoiDialog = false
                                    newPoiName = ""
                                    selectedProgramsForAdd = emptySet()
                                } else {
                                    // Handle error, e.g., post to state for UI toast
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showAddPoiDialog = false },
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showEditPoiDialog?.let { poiWithPrograms ->
                AlertDialog(
                    onDismissRequest = { showEditPoiDialog = null },
                    title = { Text("Edit POI") },
                    text = {
                        Column {
                            TextField(
                                value = editPoiName,
                                onValueChange = { editPoiName = it },
                                label = { Text("POI Name") }
                            )
                            Text("Select Programs:")
                            LazyColumn {
                                items(programsState.let { state ->
                                    if (state is AppState.Success) state.data else emptyList()
                                }) { program ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedProgramsForEdit.contains(program.id),
                                            onCheckedChange = { checked ->
                                                selectedProgramsForEdit = if (checked) {
                                                    selectedProgramsForEdit + program.id
                                                } else {
                                                    selectedProgramsForEdit - program.id
                                                }
                                            }
                                        )
                                        Text(program.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editPoiName.isNotBlank() && selectedProgramsForEdit.isNotEmpty()) {
                                    viewModel.updatePoi(poiWithPrograms.poi.copy(name = editPoiName), selectedProgramsForEdit.toList())
                                    showEditPoiDialog = null
                                    editPoiName = ""
                                    selectedProgramsForEdit = emptySet()
                                } else {
                                    // Handle error
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showEditPoiDialog = null },
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else if (selectedTab == "Tasks") {
            Text(
                text = "POI Tasks",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (val state = tasksState) {
                is AppState.Loading -> Text("Loading Tasks...")
                is AppState.Success -> {
                    LazyColumn {
                        items(state.data) { taskWithPois ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(taskWithPois.task.name)
                                    Text("POI: ${taskWithPois.pois.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { showEditTaskDialog = taskWithPois }) {
                                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { selectedTaskToDelete = taskWithPois.task }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text("Error: ${state.message}")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAddTaskDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
                Text("Add Task", modifier = Modifier.padding(start = 4.dp))
            }

            if (showAddTaskDialog) {
                AlertDialog(
                    onDismissRequest = { showAddTaskDialog = false },
                    title = { Text("Add Task") },
                    text = {
                        Column {
                            TextField(
                                value = newTaskName,
                                onValueChange = { newTaskName = it },
                                label = { Text("Task Name") }
                            )
                            Text("Select POIs:")
                            LazyColumn {
                                items(poisSimple) { poi ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedPoisForAdd.contains(poi.id),
                                            onCheckedChange = { checked ->
                                                selectedPoisForAdd = if (checked) {
                                                    selectedPoisForAdd + poi.id
                                                } else {
                                                    selectedPoisForAdd - poi.id
                                                }
                                            }
                                        )
                                        Text(poi.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newTaskName.isNotBlank() && selectedPoisForAdd.isNotEmpty()) {
                                    viewModel.insertTask(PeclTaskEntity(name = newTaskName), selectedPoisForAdd.toList())
                                    showAddTaskDialog = false
                                    newTaskName = ""
                                    selectedPoisForAdd = emptySet()
                                } else {
                                    // Handle error
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showAddTaskDialog = false },
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showEditTaskDialog?.let { taskWithPois ->
                AlertDialog(
                    onDismissRequest = { showEditTaskDialog = null },
                    title = { Text("Edit Task") },
                    text = {
                        Column {
                            TextField(
                                value = editTaskName,
                                onValueChange = { editTaskName = it },
                                label = { Text("Task Name") }
                            )
                            Text("Select POIs:")
                            LazyColumn {
                                items(poisSimple) { poi ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedPoisForEdit.contains(poi.id),
                                            onCheckedChange = { checked ->
                                                selectedPoisForEdit = if (checked) {
                                                    selectedPoisForEdit + poi.id
                                                } else {
                                                    selectedPoisForEdit - poi.id
                                                }
                                            }
                                        )
                                        Text(poi.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editTaskName.isNotBlank() && selectedPoisForEdit.isNotEmpty()) {
                                    viewModel.updateTask(taskWithPois.task.copy(name = editTaskName), selectedPoisForEdit.toList())
                                    showEditTaskDialog = null
                                    editTaskName = ""
                                    selectedPoisForEdit = emptySet()
                                } else {
                                    // Handle error
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showEditTaskDialog = null },
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else if (selectedTab == "Sub Tasks") {
            Text(
                text = "Sub Tasks - Questions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (val state = questionsState) {
                is AppState.Loading -> Text("Loading...")
                is AppState.Success -> {
                    val sortedQuestions = state.data.sortedBy { it.subTask }
                    LazyColumn {
                        items(sortedQuestions) { question ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(question.subTask)
                                    Text("Task ID: ${question.task_id}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { showEditQuestionDialog = question }) {
                                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { selectedQuestionToDelete = question }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text("Error: ${state.message}")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAddQuestionDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Question")
                }
                Text("Add Question", modifier = Modifier.padding(start = 4.dp))
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
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newSubTask.isNotBlank()) {
                                    viewModel.insertQuestion(PeclQuestionEntity(task_id = 0L, subTask = newSubTask, controlType = newControlType, scale = newScale, criticalTask = newCriticalTask), 0L)
                                    showAddQuestionDialog = false
                                    newSubTask = ""
                                    newControlType = ""
                                    newScale = ""
                                    newCriticalTask = ""
                                } else {
                                    // Handle error
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showAddQuestionDialog = false },
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showEditQuestionDialog?.let { question ->
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
                            TextField(
                                value = editControlType,
                                onValueChange = { editControlType = it },
                                label = { Text("Control Type") }
                            )
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                TextField(
                                    readOnly = true,
                                    value = editScale,
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
                                                    editScale = scale.scaleName
                                                    expanded = false
                                                }
                                            )
                                        }
                                        is AppState.Error -> Text("Error: ${state.message}")
                                    }
                                }
                            }
                            TextField(
                                value = editCriticalTask,
                                onValueChange = { editCriticalTask = it },
                                label = { Text("Critical Task") }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editSubTask.isNotBlank()) {
                                    viewModel.updateQuestion(question.copy(subTask = editSubTask, controlType = editControlType, scale = editScale, criticalTask = editCriticalTask), 0L)
                                    showEditQuestionDialog = null
                                    editSubTask = ""
                                    editControlType = ""
                                    editScale = ""
                                    editCriticalTask = ""
                                } else {
                                    // Handle error
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showEditQuestionDialog = null },
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }

    selectedProgramToDelete?.let { program ->
        AlertDialog(
            onDismissRequest = { selectedProgramToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this program?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteProgram(program)
                    selectedProgramToDelete = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { selectedProgramToDelete = null }) {
                    Text("No")
                }
            }
        )
    }

    selectedPoiToDelete?.let { poi ->
        AlertDialog(
            onDismissRequest = { selectedPoiToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this POI?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deletePoi(poi)
                    selectedPoiToDelete = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { selectedPoiToDelete = null }) {
                    Text("No")
                }
            }
        )
    }

    selectedTaskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { selectedTaskToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this task?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteTask(task)
                    selectedTaskToDelete = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { selectedTaskToDelete = null }) {
                    Text("No")
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
                Button(onClick = {
                    viewModel.deleteQuestion(question, 0L)
                    selectedQuestionToDelete = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { selectedQuestionToDelete = null }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun PeclModuleNavButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFE57373) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp, focusedElevation = 0.dp, hoveredElevation = 0.dp, disabledElevation = 0.dp)
    ) {
        Text(text)
    }
}