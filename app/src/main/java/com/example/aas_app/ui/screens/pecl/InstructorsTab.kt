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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.InstructorProgramAssignmentEntity
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.DemographicsViewModel
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorsTab(
    navController: NavController,
    demographicsViewModel: DemographicsViewModel,
    peclViewModel: PeclViewModel,
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    val instructorsState by demographicsViewModel.instructorsState.observeAsState(AppState.Success(emptyList()))
    val programsState by demographicsViewModel.programsState.observeAsState(AppState.Success(emptyList()))
    val studentsState by peclViewModel.studentsState.observeAsState(AppState.Success(emptyList()))
    var showInstructorDeleteDialog by remember { mutableStateOf(false) }
    var showAddInstructorDialog by remember { mutableStateOf(false) }
    var newInstructorName by remember { mutableStateOf("") }
    var selectedStudentsForAddInstructor by remember { mutableStateOf(setOf<Long>()) }
    var selectedProgramForAddInstructor by remember { mutableStateOf<Long?>(null) }
    var expandedProgramAddInstructor by remember { mutableStateOf(false) }
    var showEditInstructorDialog by remember { mutableStateOf(false) }
    var editInstructorName by remember { mutableStateOf("") }
    var selectedStudentsForEdit by remember { mutableStateOf(setOf<Long>()) }
    var selectedProgramForEditInstructor by remember { mutableStateOf<Long?>(null) }
    var expandedProgramEditInstructor by remember { mutableStateOf(false) }
    var selectedInstructorToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var editInstructor by remember { mutableStateOf<UserEntity?>(null) }
    var showDeleteErrorDialog by remember { mutableStateOf(false) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedInstructorForAssign by remember { mutableStateOf<UserEntity?>(null) }
    var selectedStudentsForAssign by remember { mutableStateOf(setOf<Long>()) }
    var selectedProgramForAssign by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        try {
            demographicsViewModel.loadInstructors()
            demographicsViewModel.loadPrograms()
            peclViewModel.loadStudents()
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading instructors: ${e.message}")
            }
        }
    }

    LaunchedEffect(editInstructor) {
        editInstructor?.let { instructor: UserEntity ->
            editInstructorName = instructor.fullName
            coroutineScope.launch {
                try {
                    val assignments = demographicsViewModel.getAssignmentsForInstructorSync(instructor.id)
                    selectedStudentsForEdit = assignments.map { it.student_id }.toSet<Long>()
                    val programIds = demographicsViewModel.getProgramIdsForInstructorSync(instructor.id)
                    selectedProgramForEditInstructor = programIds.firstOrNull()
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error loading assignments: ${e.message}")
                    }
                }
            }
        }
    }

    LaunchedEffect(selectedInstructorForAssign) {
        selectedInstructorForAssign?.let { instructor: UserEntity ->
            coroutineScope.launch {
                try {
                    val assignments = demographicsViewModel.getAssignmentsForInstructorSync(instructor.id)
                    selectedStudentsForAssign = assignments.map { it.student_id }.toSet<Long>()
                    val programIds = demographicsViewModel.getProgramIdsForInstructorSync(instructor.id)
                    selectedProgramForAssign = programIds.firstOrNull()
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error loading assignments: ${e.message}")
                    }
                }
            }
        }
    }

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

    when (val state = instructorsState) {
        is AppState.Loading -> CircularProgressIndicator()
        is AppState.Success -> {
            if (state.data.isEmpty()) {
                Text("No Instructors have been entered in the database. Add Instructors to begin.")
            } else {
                LazyColumn {
                    items(state.data.sortedBy { it.fullName }) { instructor ->
                        val programIds = remember { mutableStateOf<List<Long>>(emptyList()) }
                        val programsForInstructor = remember { mutableStateOf<List<PeclProgramEntity>>(emptyList()) }
                        LaunchedEffect(instructor.id) {
                            try {
                                programIds.value = demographicsViewModel.getProgramIdsForInstructorSync(instructor.id)
                                val localProgramsState = programsState
                                if (localProgramsState is AppState.Success) {
                                    programsForInstructor.value = localProgramsState.data.filter { program -> program.id in programIds.value }.sortedBy { program -> program.name }
                                }
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error loading programs: ${e.message}")
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = instructor.fullName)
                                Text(
                                    text = "Programs: ${programsForInstructor.value.joinToString { it.name } ?: "None"}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = {
                                editInstructor = instructor
                                showEditInstructorDialog = true
                            }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                selectedInstructorForAssign = instructor
                                showAssignDialog = true
                            }) {
                                Icon(imageVector = Icons.Filled.Person, contentDescription = "Assign Students")
                            }
                            IconButton(onClick = {
                                selectedInstructorToDelete = instructor
                                showInstructorDeleteDialog = true
                            }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
        is AppState.Error -> Text("Error: ${state.message}")
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
                                val hasProgram = selectedProgramForEditInstructor != null
                                if (hasProgram) {
                                    showDeleteErrorDialog = true
                                } else {
                                    val canDelete = demographicsViewModel.canDeleteInstructor(instructor.id)
                                    if (canDelete) {
                                        demographicsViewModel.deleteUser(instructor)
                                        demographicsViewModel.loadInstructors()
                                        Toast.makeText(context, "Instructor deleted successfully", Toast.LENGTH_SHORT).show()
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Cannot delete instructor with assigned students.")
                                        }
                                    }
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
            title = { Text("Remove Assignment?") },
            text = { Text("This instructor is assigned to a program. Remove the assignment and proceed with deletion?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            selectedInstructorToDelete?.let { instructor ->
                                demographicsViewModel.deleteInstructorProgramAssignmentsForInstructor(instructor.id)
                                val canDelete = demographicsViewModel.canDeleteInstructor(instructor.id)
                                if (canDelete) {
                                    demographicsViewModel.deleteUser(instructor)
                                    demographicsViewModel.loadInstructors()
                                    Toast.makeText(context, "Instructor deleted successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Cannot delete instructor with assigned students.")
                                    }
                                }
                            }
                            showDeleteErrorDialog = false
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
                    onClick = { showDeleteErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("No")
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
                    when (val localStudentsState = studentsState) {
                        is AppState.Loading -> Text("Loading...")
                        is AppState.Success<List<PeclStudentEntity>> -> {
                            LazyColumn {
                                items(localStudentsState.data) { student ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedStudentsForAddInstructor.contains(student.id),
                                            onCheckedChange = { checked ->
                                                selectedStudentsForAddInstructor = if (checked) {
                                                    selectedStudentsForAddInstructor + student.id
                                                } else {
                                                    selectedStudentsForAddInstructor - student.id
                                                }
                                            }
                                        )
                                        Text(student.fullName)
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error: ${localStudentsState.message}")
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedProgramAddInstructor,
                        onExpandedChange = { expandedProgramAddInstructor = !expandedProgramAddInstructor }
                    ) {
                        TextField(
                            readOnly = true,
                            value = when (val localProgramsState = programsState) {
                                is AppState.Success -> localProgramsState.data.find { program -> program.id == selectedProgramForAddInstructor }?.name ?: ""
                                else -> ""
                            },
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
                            when (val localProgramsState = programsState) {
                                is AppState.Success -> {
                                    localProgramsState.data.forEach { program ->
                                        DropdownMenuItem(
                                            text = { Text(program.name) },
                                            onClick = {
                                                selectedProgramForAddInstructor = program.id
                                                expandedProgramAddInstructor = false
                                            }
                                        )
                                    }
                                }
                                else -> {}
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
                            if (fullName.isNotBlank()) {
                                val nameParts = fullName.split(", ").let { parts ->
                                    if (parts.size >= 2) parts[1] to parts[0] else "" to fullName
                                }
                                val newUser = UserEntity(
                                    firstName = nameParts.first,
                                    lastName = nameParts.second,
                                    fullName = fullName,
                                    grade = "",
                                    pin = null,
                                    role = "instructor"
                                )
                                try {
                                    demographicsViewModel.insertUser(newUser) { instructorId ->
                                        if (instructorId != -1L) {
                                            selectedStudentsForAddInstructor.forEach { studentId ->
                                                selectedProgramForAddInstructor?.let { programId ->
                                                    demographicsViewModel.insertAssignment(
                                                        InstructorStudentAssignmentEntity(
                                                            instructor_id = instructorId,
                                                            student_id = studentId,
                                                            program_id = programId
                                                        )
                                                    )
                                                }
                                            }
                                            selectedProgramForAddInstructor?.let { programId ->
                                                demographicsViewModel.insertInstructorProgramAssignment(
                                                    InstructorProgramAssignmentEntity(
                                                        instructor_id = instructorId,
                                                        program_id = programId
                                                    )
                                                )
                                            }
                                            demographicsViewModel.loadInstructors()
                                            showAddInstructorDialog = false
                                            newInstructorName = ""
                                            selectedStudentsForAddInstructor = emptySet()
                                            selectedProgramForAddInstructor = null
                                            Toast.makeText(context, "Instructor added successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            snackbarHostState.showSnackbar("Error adding instructor")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("InstructorsTab", "Error adding instructor: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error adding instructor: ${e.message}")
                                }
                            } else {
                                snackbarHostState.showSnackbar("Instructor name is required")
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
                    when (val localStudentsState = studentsState) {
                        is AppState.Loading -> Text("Loading...")
                        is AppState.Success<List<PeclStudentEntity>> -> {
                            LazyColumn {
                                items(localStudentsState.data) { student ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedStudentsForEdit.contains(student.id),
                                            onCheckedChange = { checked ->
                                                selectedStudentsForEdit = if (checked) {
                                                    selectedStudentsForEdit + student.id
                                                } else {
                                                    selectedStudentsForEdit - student.id
                                                }
                                            }
                                        )
                                        Text(student.fullName)
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error: ${localStudentsState.message}")
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedProgramEditInstructor,
                        onExpandedChange = { expandedProgramEditInstructor = !expandedProgramEditInstructor }
                    ) {
                        TextField(
                            readOnly = true,
                            value = when (val localProgramsState = programsState) {
                                is AppState.Success -> localProgramsState.data.find { program -> program.id == selectedProgramForEditInstructor }?.name ?: ""
                                else -> ""
                            },
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
                            when (val localProgramsState = programsState) {
                                is AppState.Success -> {
                                    localProgramsState.data.forEach { program ->
                                        DropdownMenuItem(
                                            text = { Text(program.name) },
                                            onClick = {
                                                selectedProgramForEditInstructor = program.id
                                                expandedProgramEditInstructor = false
                                            }
                                        )
                                    }
                                }
                                else -> {}
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
                                selectedStudentsForEdit.forEach { studentId ->
                                    if (selectedProgramForEditInstructor != null) {
                                        demographicsViewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructor.id, student_id = studentId, program_id = selectedProgramForEditInstructor!!))
                                    }
                                }
                                if (selectedProgramForEditInstructor != null) {
                                    demographicsViewModel.insertInstructorProgramAssignment(InstructorProgramAssignmentEntity(instructor_id = instructor.id, program_id = selectedProgramForEditInstructor!!))
                                }
                                showEditInstructorDialog = false
                                Toast.makeText(context, "Instructor updated successfully", Toast.LENGTH_SHORT).show()
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
                    when (val localStudentsState = studentsState) {
                        is AppState.Loading -> Text("Loading...")
                        is AppState.Success<List<PeclStudentEntity>> -> {
                            LazyColumn {
                                items(localStudentsState.data) { student ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedStudentsForAssign.contains(student.id),
                                            onCheckedChange = { checked ->
                                                selectedStudentsForAssign = if (checked) {
                                                    selectedStudentsForAssign + student.id
                                                } else {
                                                    selectedStudentsForAssign - student.id
                                                }
                                            }
                                        )
                                        Text(student.fullName)
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error: ${localStudentsState.message}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            selectedInstructorForAssign?.let { instructor: UserEntity ->
                                val instructorProgramId = selectedProgramForAssign
                                if (instructorProgramId == null) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Instructor has no assigned program")
                                    }
                                    return@launch
                                }
                                demographicsViewModel.deleteAssignmentsForInstructor(instructor.id)
                                selectedStudentsForAssign.forEach { studentId ->
                                    demographicsViewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructor.id, student_id = studentId, program_id = instructorProgramId))
                                }
                                demographicsViewModel.loadInstructors()
                                showAssignDialog = false
                                selectedStudentsForAssign = emptySet()
                                Toast.makeText(context, "Students assigned successfully", Toast.LENGTH_SHORT).show()
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
}