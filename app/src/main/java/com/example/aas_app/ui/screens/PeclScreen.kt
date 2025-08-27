package com.example.aas_app.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.InstructorProgramAssignmentEntity
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.DemographicsViewModel
import com.example.aas_app.viewmodel.PeclViewModel
import com.example.aas_app.viewmodel.PoiWithPrograms
import com.example.aas_app.viewmodel.TaskWithPois
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeclScreen(navController: NavController) {
    val adminViewModel: AdminViewModel = hiltViewModel()
    val demographicsViewModel: DemographicsViewModel = hiltViewModel()
    val peclViewModel: PeclViewModel = hiltViewModel()
    val context = LocalContext.current
    val programsState by adminViewModel.programsState.observeAsState(AppState.Success(emptyList<PeclProgramEntity>()) as AppState<List<PeclProgramEntity>>)
    val poisState by adminViewModel.poisState.observeAsState(AppState.Success(emptyList<PoiWithPrograms>()) as AppState<List<PoiWithPrograms>>)
    val tasksState by adminViewModel.tasksState.observeAsState(AppState.Success(emptyList<TaskWithPois>()) as AppState<List<TaskWithPois>>)
    val questionsState by adminViewModel.questionsState.observeAsState(AppState.Success(emptyList<PeclQuestionEntity>()) as AppState<List<PeclQuestionEntity>>)
    val scalesState by adminViewModel.scalesState.observeAsState(AppState.Success(emptyList<ScaleEntity>()) as AppState<List<ScaleEntity>>)
    val instructors by demographicsViewModel.instructorsWithPrograms.observeAsState(emptyList())
    val studentsState by peclViewModel.studentsState.observeAsState(AppState.Success(emptyList<PeclStudentEntity>()) as AppState<List<PeclStudentEntity>>)
    val programs by demographicsViewModel.programs.observeAsState(emptyList())
    val poisSimple by adminViewModel.poisSimple.observeAsState(emptyList())
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
    var showTaskDeleteDialog by remember { mutableStateOf(false) } // Separate state for task delete
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    var newSubTask by remember { mutableStateOf("") }
    var newControlType by remember { mutableStateOf("") }
    var newScale by remember { mutableStateOf("") }
    var newCriticalTask by remember { mutableStateOf("") }
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
    var expandedCriticalTaskEdit by remember { mutableStateOf(false) }
    var showInstructorDeleteDialog by remember { mutableStateOf(false) } // Separate state for instructor delete
    var showAddInstructorDialog by remember { mutableStateOf(false) }
    var newInstructorName by remember { mutableStateOf("") }
    var selectedStudentsForAddInstructor by remember { mutableStateOf(setOf<Long>()) }
    var selectedProgramForAddInstructor by remember { mutableStateOf<Long?>(null) }
    var expandedProgramAddInstructor by remember { mutableStateOf(false) }
    var showEditInstructorDialog by remember { mutableStateOf(false) }
    var editInstructorName by remember { mutableStateOf("") }
    var selectedStudentsForEditInstructor by remember { mutableStateOf(setOf<Long>()) }
    var selectedProgramForEditInstructor by remember { mutableStateOf<Long?>(null) }
    var expandedProgramEditInstructor by remember { mutableStateOf(false) }
    var selectedInstructorToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var editInstructor by remember { mutableStateOf<UserEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteErrorDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var newStudentFirstName by remember { mutableStateOf("") }
    var newStudentLastName by remember { mutableStateOf("") }
    var newStudentGrade by remember { mutableStateOf("") }
    var newStudentPin by remember { mutableStateOf<Int?>(null) }
    var showEditStudentDialog by remember { mutableStateOf(false) }
    var editStudent by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var editStudentFirstName by remember { mutableStateOf("") }
    var editStudentLastName by remember { mutableStateOf("") }
    var editStudentGrade by remember { mutableStateOf("") }
    var editStudentPin by remember { mutableStateOf<Int?>(null) }
    var selectedStudentToDelete by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var showStudentDeleteDialog by remember { mutableStateOf(false) } // Separate state for student delete
    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedInstructorForAssign by remember { mutableStateOf<UserEntity?>(null) }
    var expandedProgramAssign by remember { mutableStateOf(false) }
    var selectedStudentsForAssign by remember { mutableStateOf(setOf<Long>()) }
    var selectedProgramForAssign by remember { mutableStateOf<Long?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val controlTypeOptions = listOf("CheckBox", "ComboBox", "Comment", "ListBox", "OptionButton", "ScoreBox", "TextBox")
    val criticalTaskOptions = listOf("No", "Yes")

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Programs") {
            adminViewModel.loadPrograms()
        } else if (selectedTab == "POI") {
            adminViewModel.loadPrograms()
            adminViewModel.loadAllPoisWithPrograms()
        } else if (selectedTab == "Tasks") {
            adminViewModel.loadAllPois()
            adminViewModel.loadAllTasksWithPois()
        } else if (selectedTab == "Sub Tasks") {
            adminViewModel.loadAllQuestions()
            adminViewModel.loadScales()
            adminViewModel.loadAllTasksWithPois()
        } else if (selectedTab == "Instructors") {
            demographicsViewModel.loadInstructors()
            demographicsViewModel.loadPrograms()
            peclViewModel.loadStudents()
        } else if (selectedTab == "Students") {
            peclViewModel.loadStudents()
        }
    }

    LaunchedEffect(showAddInstructorDialog) {
        if (showAddInstructorDialog) {
            Toast.makeText(context, "Opening Add Instructor dialog", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(showEditPoiDialog) {
        showEditPoiDialog?.let { poiWithPrograms ->
            editPoiName = poiWithPrograms.poi.name
            selectedProgramsForEdit = poiWithPrograms.programs.mapNotNull { programName ->
                if (programsState is AppState.Success) {
                    (programsState as AppState.Success).data.find { it.name == programName }?.id
                } else null
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
            editTaskId = question.task_id
        }
    }

    LaunchedEffect(editInstructor) {
        editInstructor?.let { instructor ->
            editInstructorName = instructor.fullName
            coroutineScope.launch {
                val assignments = demographicsViewModel.getAssignmentsForInstructor(instructor.id).value ?: emptyList()
                selectedStudentsForEditInstructor = assignments.map { assignment ->
                    assignment.student_id
                }.toSet()
                selectedProgramForEditInstructor = assignments.firstOrNull()?.program_id
            }
        }
    }

    LaunchedEffect(selectedInstructorForAssign) {
        selectedInstructorForAssign?.let { instructor ->
            coroutineScope.launch {
                val assignments = demographicsViewModel.getAssignmentsForInstructor(instructor.id).value ?: emptyList()
                selectedStudentsForAssign = assignments.map { assignment ->
                    assignment.student_id
                }.toSet()
                selectedProgramForAssign = assignments.firstOrNull()?.program_id
            }
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
            PeclModuleNavButton("Instructors", selectedTab == "Instructors") { selectedTab = "Instructors" }
            PeclModuleNavButton("Students", selectedTab == "Students") { selectedTab = "Students" }
        }

        errorMessage?.let {
            Text(text = it, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (selectedTab == "Programs") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Programs",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddProgram = !showAddProgram }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Program")
                }
                Text("Add Program", modifier = Modifier.padding(start = 4.dp))
            }
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
                                        adminViewModel.updateProgram(program.copy(name = editName))
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
                                    Text(text = program.name, modifier = Modifier.weight(1f))
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

            if (showAddProgram) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newProgramName,
                        onValueChange = { newProgramName = it },
                        label = { Text("New Program Name") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newProgramName.isNotBlank()) {
                            adminViewModel.insertProgram(PeclProgramEntity(0L, newProgramName))
                            newProgramName = ""
                            showAddProgram = false
                            Toast.makeText(context, "Program added successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = "Program name cannot be blank"
                        }
                    }) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = "Save New Program")
                    }
                }
            }
        } else if (selectedTab == "POI") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Program of Instruction",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddPoiDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add POI")
                }
                Text("Add POI", modifier = Modifier.padding(start = 4.dp))
            }

            when (val state = poisState) {
                is AppState.Loading -> Text("Loading POIs...")
                is AppState.Success -> {
                    LazyColumn {
                        items(state.data) { poiWithPrograms ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = poiWithPrograms.poi.name)
                                    Text(text = "Programs: ${poiWithPrograms.programs.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
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
                            Text(text = "Select Programs:")
                            LazyColumn {
                                items(if (programsState is AppState.Success) (programsState as AppState.Success).data else emptyList()) { program: PeclProgramEntity ->
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
                                        Text(text = program.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newPoiName.isNotBlank() && selectedProgramsForAdd.isNotEmpty()) {
                                    adminViewModel.insertPoi(PeclPoiEntity(name = newPoiName), selectedProgramsForAdd.toList())
                                    showAddPoiDialog = false
                                    newPoiName = ""
                                    selectedProgramsForAdd = emptySet()
                                    Toast.makeText(context, "POI added successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = "POI name and at least one program are required"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp),
                            enabled = newPoiName.isNotBlank() && selectedProgramsForAdd.isNotEmpty()
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showAddPoiDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showEditPoiDialog?.let { poiWithPrograms: PoiWithPrograms ->
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
                            Text(text = "Select Programs:")
                            LazyColumn {
                                items(if (programsState is AppState.Success) (programsState as AppState.Success).data else emptyList()) { program: PeclProgramEntity ->
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
                                        Text(text = program.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editPoiName.isNotBlank() && selectedProgramsForEdit.isNotEmpty()) {
                                    adminViewModel.updatePoi(poiWithPrograms.poi.copy(name = editPoiName), selectedProgramsForEdit.toList())
                                    showEditPoiDialog = null
                                    editPoiName = ""
                                    selectedProgramsForEdit = emptySet()
                                    Toast.makeText(context, "POI updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = "POI name and at least one program are required"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp),
                            enabled = editPoiName.isNotBlank() && selectedProgramsForEdit.isNotEmpty()
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showEditPoiDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else if (selectedTab == "Tasks") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "POI Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddTaskDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task")
                }
                Text("Add Task", modifier = Modifier.padding(start = 4.dp))
            }

            when (val state = tasksState) {
                is AppState.Loading -> Text("Loading...")
                is AppState.Success -> {
                    LazyColumn {
                        items(state.data) { taskWithPois ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = taskWithPois.task.name)
                                    Text(text = "POI: ${taskWithPois.pois.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { showEditTaskDialog = taskWithPois }) {
                                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    selectedTaskToDelete = taskWithPois.task
                                    showTaskDeleteDialog = true
                                }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text("Error: ${state.message}")
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
                            Text(text = "Select POIs:")
                            LazyColumn {
                                items(poisSimple) { poi: PeclPoiEntity ->
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
                                        Text(text = poi.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newTaskName.isNotBlank() && selectedPoisForAdd.isNotEmpty()) {
                                    adminViewModel.insertTask(PeclTaskEntity(name = newTaskName), selectedPoisForAdd.toList())
                                    showAddTaskDialog = false
                                    newTaskName = ""
                                    selectedPoisForAdd = emptySet()
                                    Toast.makeText(context, "Task added successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = "Task name and at least one POI are required"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp),
                            enabled = newTaskName.isNotBlank() && selectedPoisForAdd.isNotEmpty()
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showAddTaskDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
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
                            Text(text = "Select POIs:")
                            LazyColumn {
                                items(poisSimple) { poi: PeclPoiEntity ->
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
                                        Text(text = poi.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editTaskName.isNotBlank() && selectedPoisForEdit.isNotEmpty()) {
                                    adminViewModel.updateTask(taskWithPois.task.copy(name = editTaskName), selectedPoisForEdit.toList())
                                    showEditTaskDialog = null
                                    editTaskName = ""
                                    selectedPoisForEdit = emptySet()
                                    Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    errorMessage = "Task name and at least one POI are required"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp),
                            enabled = editTaskName.isNotBlank() && selectedPoisForEdit.isNotEmpty()
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showEditTaskDialog = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } else if (selectedTab == "Sub Tasks") {
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

            when (val state = questionsState) {
                is AppState.Loading -> Text("Loading...")
                is AppState.Success -> {
                    val sortedQuestions = state.data.sortedBy { it.subTask }
                    val taskMap = if (tasksState is AppState.Success) {
                        (tasksState as AppState.Success).data.associate { it.task.id to it.task.name }
                    } else emptyMap()
                    if (sortedQuestions.isEmpty()) {
                        Text("No questions available")
                    } else {
                        LazyColumn {
                            items(sortedQuestions) { question ->
                                val taskName = question.task_id?.let { taskMap[it] } ?: "Unassigned"
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = question.subTask)
                                        Text(text = "Task: $taskName, ControlType: ${question.controlType}, Scale: ${question.scale}, CriticalTask: ${question.criticalTask}", style = MaterialTheme.typography.bodySmall)
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
                                            is AppState.Success -> state.data.sortedBy { it.scaleName }.forEach { scale ->
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
                                        (tasksState as AppState.Success).data.sortedBy { it.task.name }.find { it.task.id == newTaskId }?.task?.name ?: ""
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
                                        is AppState.Success -> state.data.sortedBy { it.task.name }.forEach { taskWithPois ->
                                            DropdownMenuItem(
                                                text = { Text(taskWithPois.task.name) },
                                                onClick = {
                                                    newTaskId = taskWithPois.task.id
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
                                        adminViewModel.insertQuestion(PeclQuestionEntity(task_id = taskIdValue, subTask = newSubTask, controlType = newControlType, scale = newScale, criticalTask = newCriticalTask), taskIdValue)
                                        showAddQuestionDialog = false
                                        newSubTask = ""
                                        newControlType = ""
                                        newScale = ""
                                        newCriticalTask = ""
                                        newTaskId = null
                                        Toast.makeText(context, "Question added successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        errorMessage = "Sub task is required"
                                    }
                                } else {
                                    errorMessage = "Task assignment is required"
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
                                            is AppState.Success -> state.data.sortedBy { it.scaleName }.forEach { scale ->
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
                                        (tasksState as AppState.Success).data.sortedBy { it.task.name }.find { it.task.id == editTaskId }?.task?.name ?: ""
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
                                        is AppState.Success -> state.data.sortedBy { it.task.name }.forEach { taskWithPois ->
                                            DropdownMenuItem(
                                                text = { Text(taskWithPois.task.name) },
                                                onClick = {
                                                    editTaskId = taskWithPois.task.id
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
                                        adminViewModel.updateQuestion(question.copy(task_id = taskIdValue, subTask = editSubTask, controlType = editControlType, scale = editScale, criticalTask = editCriticalTask), taskIdValue)
                                        showEditQuestionDialog = null
                                        editSubTask = ""
                                        editControlType = ""
                                        editScale = ""
                                        editCriticalTask = ""
                                        editTaskId = null
                                        Toast.makeText(context, "Question updated successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        errorMessage = "Sub task is required"
                                    }
                                } else {
                                    errorMessage = "Task assignment is required"
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
        } else if (selectedTab == "Instructors") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instructors",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddInstructorDialog = true }) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Instructor")
                }
                Text("Add Instructors", modifier = Modifier.padding(start = 4.dp))
            }

            val sortedInstructors = instructors.sortedBy { it.instructor.fullName }
            if (sortedInstructors.isEmpty()) {
                Text("No Instructors have been entered in the database. Add Instructors to begin.")
            } else {
                LazyColumn {
                    items(sortedInstructors) { instructorWithProgram ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = instructorWithProgram.instructor.fullName)
                                Text(text = "Programs: ${instructorWithProgram.programName ?: "None"}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = {
                                editInstructor = instructorWithProgram.instructor
                                showEditInstructorDialog = true
                            }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                selectedInstructorForAssign = instructorWithProgram.instructor
                                showAssignDialog = true
                            }) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = "Assign Students")
                            }
                            IconButton(onClick = { selectedInstructorToDelete = instructorWithProgram.instructor; showInstructorDeleteDialog = true }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == "Students") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PECL Students",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAddStudentDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Student")
                }
                Text("Add Student", modifier = Modifier.padding(start = 4.dp))
            }

            when (val state = studentsState) {
                is AppState.Loading -> Text("Loading...")
                is AppState.Success -> {
                    val sortedStudents = state.data.sortedBy { it.fullName }
                    LazyColumn {
                        items(sortedStudents) { student ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = student.fullName, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    editStudent = student
                                    editStudentFirstName = student.firstName
                                    editStudentLastName = student.lastName
                                    editStudentGrade = student.grade
                                    editStudentPin = student.pin
                                    showEditStudentDialog = true
                                }) {
                                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = {
                                    selectedStudentToDelete = student
                                    showStudentDeleteDialog = true
                                }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text("Error: ${state.message}")
            }
        }
    }

    selectedProgramToDelete?.let { program ->
        AlertDialog(
            onDismissRequest = { selectedProgramToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this program?") },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteProgram(program)
                        selectedProgramToDelete = null
                        Toast.makeText(context, "Program deleted successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedProgramToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
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
                Button(
                    onClick = {
                        adminViewModel.deletePoi(poi)
                        selectedPoiToDelete = null
                        Toast.makeText(context, "POI deleted successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedPoiToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("No")
                }
            }
        )
    }

    if (showTaskDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showTaskDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this task?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedTaskToDelete?.let { adminViewModel.deleteTask(it) }
                        showTaskDeleteDialog = false
                        Toast.makeText(context, "Task deleted successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showTaskDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
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
                Button(
                    onClick = {
                        adminViewModel.deleteQuestion(question)
                        selectedQuestionToDelete = null
                        Toast.makeText(context, "Question deleted successfully", Toast.LENGTH_SHORT).show()
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

    if (showInstructorDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showInstructorDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this instructor?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            selectedInstructorToDelete?.let { instructor ->
                                val canDelete = demographicsViewModel.canDeleteInstructor(instructor.id)
                                if (canDelete) {
                                    demographicsViewModel.deleteUser(instructor)
                                    demographicsViewModel.loadInstructors()
                                    Toast.makeText(context, "Instructor deleted successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    showDeleteErrorDialog = true
                                }
                            }
                            showInstructorDeleteDialog = false
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
                    onClick = { showInstructorDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("No")
                }
            }
        )
    }

    if (showDeleteErrorDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteErrorDialog = false },
            title = { Text("Deletion Restricted") },
            text = { Text("Cannot delete instructor with assigned students or programs. Please remove assignments first.") },
            confirmButton = {
                Button(
                    onClick = { showDeleteErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showAddInstructorDialog) {
        AlertDialog(
            onDismissRequest = { showAddInstructorDialog = false },
            title = { Text("Add Instructor") },
            text = {
                Column {
                    TextField(
                        value = newInstructorName,
                        onValueChange = { newInstructorName = it },
                        label = { Text("Instructor Name") }
                    )
                    Text(text = "Select Students:")
                    when (val state = studentsState) {
                        is AppState.Loading -> Text("Loading...")
                        is AppState.Success -> {
                            LazyColumn {
                                items(state.data) { student: PeclStudentEntity ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedStudentsForAddInstructor.contains(student.id),
                                            onCheckedChange = { checked ->
                                                selectedStudentsForAddInstructor = if (checked) selectedStudentsForAddInstructor + student.id else selectedStudentsForAddInstructor - student.id
                                            }
                                        )
                                        Text(student.fullName)
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error: ${state.message}")
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedProgramAddInstructor,
                        onExpandedChange = { expandedProgramAddInstructor = !expandedProgramAddInstructor }
                    ) {
                        TextField(
                            readOnly = true,
                            value = programs.find { it.id == selectedProgramForAddInstructor }?.name ?: "",
                            onValueChange = { },
                            label = { Text("Select Program") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgramAddInstructor) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProgramAddInstructor,
                            onDismissRequest = { expandedProgramAddInstructor = false }
                        ) {
                            programs.forEach { program ->
                                DropdownMenuItem(
                                    text = { Text(program.name) },
                                    onClick = {
                                        selectedProgramForAddInstructor = program.id
                                        expandedProgramAddInstructor = false
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
                        coroutineScope.launch {
                            val fullName = newInstructorName
                            val newUser = UserEntity(firstName = "", lastName = "", grade = "", pin = null, fullName = fullName, role = "instructor")
                            val instructorId = demographicsViewModel.insertUserSync(newUser)
                            selectedStudentsForAddInstructor.forEach { studentId ->
                                demographicsViewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructorId, student_id = studentId, program_id = selectedProgramForAddInstructor))
                            }
                            if (selectedProgramForAddInstructor != null) {
                                demographicsViewModel.insertInstructorProgramAssignment(InstructorProgramAssignmentEntity(instructor_id = instructorId, program_id = selectedProgramForAddInstructor!!))
                            }
                            demographicsViewModel.loadInstructors()
                            showAddInstructorDialog = false
                            newInstructorName = ""
                            selectedStudentsForAddInstructor = emptySet()
                            selectedProgramForAddInstructor = null
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
                    onClick = { showAddInstructorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditInstructorDialog) {
        AlertDialog(
            onDismissRequest = { showEditInstructorDialog = false },
            title = { Text("Edit Instructor") },
            text = {
                Column {
                    TextField(
                        value = editInstructorName,
                        onValueChange = { editInstructorName = it },
                        label = { Text("Instructor Name") }
                    )
                    Text(text = "Select Students:")
                    when (val state = studentsState) {
                        is AppState.Loading -> Text("Loading...")
                        is AppState.Success -> {
                            LazyColumn {
                                items(state.data) { student: PeclStudentEntity ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedStudentsForEditInstructor.contains(student.id),
                                            onCheckedChange = { checked ->
                                                selectedStudentsForEditInstructor = if (checked) selectedStudentsForEditInstructor + student.id else selectedStudentsForEditInstructor - student.id
                                            }
                                        )
                                        Text(student.fullName)
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error: ${state.message}")
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedProgramEditInstructor,
                        onExpandedChange = { expandedProgramEditInstructor = !expandedProgramEditInstructor }
                    ) {
                        TextField(
                            readOnly = true,
                            value = programs.find { it.id == selectedProgramForEditInstructor }?.name ?: "",
                            onValueChange = { },
                            label = { Text("Select Program") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgramEditInstructor) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProgramEditInstructor,
                            onDismissRequest = { expandedProgramEditInstructor = false }
                        ) {
                            programs.forEach { program ->
                                DropdownMenuItem(
                                    text = { Text(program.name) },
                                    onClick = {
                                        selectedProgramForEditInstructor = program.id
                                        expandedProgramEditInstructor = false
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
                        coroutineScope.launch {
                            editInstructor?.let { instructor ->
                                val updatedUser = instructor.copy(fullName = editInstructorName)
                                demographicsViewModel.updateUser(updatedUser)
                                demographicsViewModel.deleteAssignmentsForInstructor(instructor.id)
                                selectedStudentsForEditInstructor.forEach { studentId ->
                                    demographicsViewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructor.id, student_id = studentId, program_id = selectedProgramForEditInstructor))
                                }
                                demographicsViewModel.deleteInstructorProgramAssignmentsForInstructor(instructor.id)
                                if (selectedProgramForEditInstructor != null) {
                                    demographicsViewModel.insertInstructorProgramAssignment(InstructorProgramAssignmentEntity(instructor_id = instructor.id, program_id = selectedProgramForEditInstructor!!))
                                }
                                demographicsViewModel.loadInstructors()
                                showEditInstructorDialog = false
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
                    onClick = { showEditInstructorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAssignDialog) {
        AlertDialog(
            onDismissRequest = { showAssignDialog = false },
            title = { Text("Assign Students to ${selectedInstructorForAssign?.fullName}") },
            text = {
                Column {
                    Text(text = "Select Students:")
                    when (val state = studentsState) {
                        is AppState.Loading -> Text("Loading...")
                        is AppState.Success -> {
                            LazyColumn {
                                items(state.data) { student: PeclStudentEntity ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedStudentsForAssign.contains(student.id),
                                            onCheckedChange = { checked ->
                                                selectedStudentsForAssign = if (checked) selectedStudentsForAssign + student.id else selectedStudentsForAssign - student.id
                                            }
                                        )
                                        Text(student.fullName)
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error: ${state.message}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            selectedInstructorForAssign?.let { instructor ->
                                val programNames = demographicsViewModel.getProgramsForInstructor(instructor.id).first()
                                val instructorProgramId = programs.firstOrNull { it.name in programNames }?.id
                                demographicsViewModel.deleteAssignmentsForInstructor(instructor.id)
                                selectedStudentsForAssign.forEach { studentId ->
                                    demographicsViewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructor.id, student_id = studentId, program_id = instructorProgramId))
                                }
                                demographicsViewModel.loadInstructors()
                                showAssignDialog = false
                                selectedStudentsForAssign = emptySet()
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
                    onClick = { showAssignDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Add Student") },
            text = {
                Column {
                    TextField(
                        value = newStudentFirstName,
                        onValueChange = { newStudentFirstName = it },
                        label = { Text("First Name") }
                    )
                    TextField(
                        value = newStudentLastName,
                        onValueChange = { newStudentLastName = it },
                        label = { Text("Last Name") }
                    )
                    TextField(
                        value = newStudentGrade,
                        onValueChange = { newStudentGrade = it },
                        label = { Text("Grade") }
                    )
                    TextField(
                        value = newStudentPin?.toString() ?: "",
                        onValueChange = { newStudentPin = it.toIntOrNull() },
                        label = { Text("PIN") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStudentFirstName.isNotBlank() && newStudentLastName.isNotBlank()) {
                            val fullName = "$newStudentLastName, $newStudentFirstName"
                            val newStudent = PeclStudentEntity(firstName = newStudentFirstName, lastName = newStudentLastName, grade = newStudentGrade, pin = newStudentPin, fullName = fullName)
                            peclViewModel.insertPeclStudent(newStudent)
                            peclViewModel.loadStudents()
                            showAddStudentDialog = false
                            newStudentFirstName = ""
                            newStudentLastName = ""
                            newStudentGrade = ""
                            newStudentPin = null
                            Toast.makeText(context, "Student added successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = "First name and last name are required"
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
                    onClick = { showAddStudentDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditStudentDialog) {
        AlertDialog(
            onDismissRequest = { showEditStudentDialog = false },
            title = { Text("Edit Student") },
            text = {
                Column {
                    TextField(
                        value = editStudentFirstName,
                        onValueChange = { editStudentFirstName = it },
                        label = { Text("First Name") }
                    )
                    TextField(
                        value = editStudentLastName,
                        onValueChange = { editStudentLastName = it },
                        label = { Text("Last Name") }
                    )
                    TextField(
                        value = editStudentGrade,
                        onValueChange = { editStudentGrade = it },
                        label = { Text("Grade") }
                    )
                    TextField(
                        value = editStudentPin?.toString() ?: "",
                        onValueChange = { editStudentPin = it.toIntOrNull() },
                        label = { Text("PIN") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fullName = "$editStudentLastName, $editStudentFirstName"
                        if (fullName.isNotBlank()) {
                            editStudent?.let { student ->
                                val updatedStudent = student.copy(firstName = editStudentFirstName, lastName = editStudentLastName, grade = editStudentGrade, pin = editStudentPin, fullName = fullName)
                                peclViewModel.updatePeclStudent(updatedStudent)
                                peclViewModel.loadStudents()
                                showEditStudentDialog = false
                                Toast.makeText(context, "Student updated successfully", Toast.LENGTH_SHORT).show()
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
                    onClick = { showEditStudentDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showStudentDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showStudentDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this student?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedStudentToDelete?.let { peclViewModel.deletePeclStudent(it) }
                        peclViewModel.loadStudents()
                        showStudentDeleteDialog = false
                        Toast.makeText(context, "Student deleted successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showStudentDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
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
        )
    ) {
        Text(text)
    }
}