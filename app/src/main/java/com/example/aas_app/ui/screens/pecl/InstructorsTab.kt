package com.example.aas_app.ui.screens.pecl

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
fun InstructorsTab(demographicsViewModel: DemographicsViewModel, peclViewModel: PeclViewModel, errorMessage: String?, snackbarHostState: SnackbarHostState, coroutineScope: CoroutineScope) {
    val context = LocalContext.current
    val instructors by demographicsViewModel.instructorsWithPrograms.observeAsState(emptyList())
    val studentsState by peclViewModel.studentsState.observeAsState(AppState.Success(emptyList<PeclStudentEntity>()))
    val programs by demographicsViewModel.programs.observeAsState(emptyList())
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

    LaunchedEffect(showAddInstructorDialog) {
        if (showAddInstructorDialog) {
            Toast.makeText(context, "Opening Add Instructor dialog", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(editInstructor) {
        editInstructor?.let { instructor: UserEntity ->
            editInstructorName = instructor.fullName
            coroutineScope.launch {
                try {
                    val assignments = demographicsViewModel.getAssignmentsForInstructorSync(instructor.id)
                    selectedStudentsForEdit = assignments.map { it.student_id }.toSet()
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
                    selectedStudentsForAssign = assignments.map { it.student_id }.toSet()
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

    if (showInstructorDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showInstructorDeleteDialog = false
            },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this instructor?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            selectedInstructorToDelete?.let { instructor ->
                                val hasProgram = selectedProgramForEditInstructor != null // Check if program is assigned
                                if (hasProgram) {
                                    // Prompt to remove program assignment
                                    showDeleteErrorDialog = true // Reuse for prompt, but update text
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
                    onClick = {
                        showInstructorDeleteDialog = false
                    },
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
            onDismissRequest = {
                showDeleteErrorDialog = false
            },
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
                    onClick = {
                        showDeleteErrorDialog = false
                    },
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
                            if (fullName.isNotBlank()) {
                                val newUser = UserEntity(firstName = "", lastName = "", grade = "", pin = null, fullName = fullName, role = "instructor")
                                val instructorId = demographicsViewModel.insertUserSync(newUser)
                                selectedStudentsForAddInstructor.forEach { studentId ->
                                    if (selectedProgramForAddInstructor != null) {
                                        demographicsViewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructorId, student_id = studentId, program_id = selectedProgramForAddInstructor))
                                    }
                                }
                                if (selectedProgramForAddInstructor != null) {
                                    demographicsViewModel.insertInstructorProgramAssignment(InstructorProgramAssignmentEntity(instructor_id = instructorId, program_id = selectedProgramForAddInstructor!!))
                                }
                                demographicsViewModel.loadInstructors()
                                showAddInstructorDialog = false
                                newInstructorName = ""
                                selectedStudentsForAddInstructor = emptySet()
                                selectedProgramForAddInstructor = null
                                Toast.makeText(context, "Instructor added successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Instructor name is required")
                                }
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
                    when (val state = studentsState) {
                        is AppState.Loading -> Text("Loading...")
                        is AppState.Success -> {
                            LazyColumn {
                                items(state.data) { student: PeclStudentEntity ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedStudentsForEdit.contains(student.id),
                                            onCheckedChange = { checked ->
                                                selectedStudentsForEdit = if (checked) selectedStudentsForEdit + student.id else selectedStudentsForEdit - student.id
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
                                selectedStudentsForEdit.forEach { studentId ->
                                    if (selectedProgramForEditInstructor != null) {
                                        demographicsViewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructor.id, student_id = studentId, program_id = selectedProgramForEditInstructor))
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