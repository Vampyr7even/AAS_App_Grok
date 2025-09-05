package com.example.aas_app.ui.screens

import android.util.Log
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
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUsersScreen(navController: NavController, role: String?) {
    val viewModel = hiltViewModel<DemographicsViewModel>()
    val usersState by viewModel.usersState.observeAsState(AppState.Loading)
    val instructorsState by viewModel.instructorsState.observeAsState(AppState.Loading)
    val studentsState by viewModel.studentsState.observeAsState(AppState.Loading)
    val programsState by viewModel.programsState.observeAsState(AppState.Loading)
    val programIdsState by viewModel.programIdsState.observeAsState(AppState.Loading)
    val instructorAssignmentsState by viewModel.instructorAssignmentsState.observeAsState(AppState.Loading)
    val instructorProgramAssignmentsState by viewModel.instructorProgramAssignmentsState.observeAsState(AppState.Loading)
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var studentToDelete by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var selectedInstructor by remember { mutableStateOf<UserEntity?>(null) }
    var selectedProgram by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var showStudentDialog by remember { mutableStateOf(false) }
    var showProgramDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (role == "instructor") {
            viewModel.loadInstructors()
            viewModel.loadPrograms()
        } else if (role == "student") {
            viewModel.loadStudents()
            viewModel.loadInstructors()
            viewModel.loadPrograms()
        } else {
            viewModel.loadUsers()
        }
    }

    LaunchedEffect(selectedInstructor) {
        selectedInstructor?.let {
            viewModel.loadInstructorAssignments(it.id)
            viewModel.loadProgramIdsForInstructor(it.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (role) {
            "instructor" -> {
                Text(
                    text = "Manage Instructors",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                when (val state = instructorsState) {
                    is AppState.Loading -> CircularProgressIndicator()
                    is AppState.Success -> {
                        val instructorAssignments = mutableListOf<InstructorStudentAssignmentEntity>()
                        val programIds = mutableListOf<Long>()
                        coroutineScope.launch {
                            try {
                                instructorAssignments.addAll(viewModel.getAssignmentsForInstructorSync(selectedInstructor?.id ?: 0L))
                                programIds.addAll(viewModel.getProgramIdsForInstructorSync(selectedInstructor?.id ?: 0L))
                            } catch (e: Exception) {
                                Log.e("UpdateUsersScreen", "Error loading assignments or program IDs: ${e.message}", e)
                                snackbarHostState.showSnackbar("Error loading data: ${e.message}")
                            }
                        }
                        val assignedStudentIds = instructorAssignments.map { it.student_id }.toSet()
                        val programs = when (val programState = programsState) {
                            is AppState.Success -> programState.data.filter { it.id in programIds }.sortedBy { program -> program.name }
                            else -> emptyList()
                        }
                        if (state.data.isEmpty()) {
                            Text(
                                text = "No instructors available.",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LazyColumn {
                                items(state.data.sortedBy { instructor -> instructor.fullName }) { instructor ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = instructor.fullName,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = programs.joinToString { program -> program.name },
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = {
                                            navController.navigate("editUser/${instructor.id}/instructor")
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit Instructor")
                                        }
                                        IconButton(onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    if (viewModel.canDeleteInstructor(instructor.id)) {
                                                        userToDelete = instructor
                                                        showDialog = true
                                                    } else {
                                                        snackbarHostState.showSnackbar("Cannot delete instructor with assigned students or programs")
                                                    }
                                                } catch (e: Exception) {
                                                    Log.e("UpdateUsersScreen", "Error checking deletion: ${e.message}", e)
                                                    snackbarHostState.showSnackbar("Error: ${e.message}")
                                                }
                                            }
                                        }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete Instructor")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is AppState.Error -> Text("Error: ${state.message}")
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = showProgramDialog,
                    onExpandedChange = { showProgramDialog = !showProgramDialog }
                ) {
                    TextField(
                        value = selectedProgram?.name ?: "Select Program",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Program") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProgramDialog) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showProgramDialog,
                        onDismissRequest = { showProgramDialog = false }
                    ) {
                        when (val state = programsState) {
                            is AppState.Success -> {
                                state.data.forEach { program ->
                                    DropdownMenuItem(
                                        text = { Text(program.name) },
                                        onClick = {
                                            selectedProgram = program
                                            showProgramDialog = false
                                        }
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = newFirstName,
                    onValueChange = { newFirstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newLastName,
                    onValueChange = { newLastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("Pin") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newFirstName.isNotBlank() && newLastName.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    val newUserId = viewModel.insertUserSync(
                                        UserEntity(
                                            firstName = newFirstName,
                                            lastName = newLastName,
                                            fullName = "$newLastName, $newFirstName",
                                            grade = "",
                                            role = "instructor",
                                            pin = newPin.toIntOrNull()
                                        )
                                    )
                                    if (newUserId != -1L && selectedProgram != null) {
                                        viewModel.insertInstructorProgramAssignment(
                                            InstructorProgramAssignmentEntity(
                                                instructor_id = newUserId,
                                                program_id = selectedProgram!!.id
                                            )
                                        )
                                    }
                                    newFirstName = ""
                                    newLastName = ""
                                    newPin = ""
                                    selectedProgram = null
                                    snackbarHostState.showSnackbar("Instructor added successfully")
                                } catch (e: Exception) {
                                    Log.e("UpdateUsersScreen", "Error adding instructor: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error adding instructor: ${e.message}")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("First and last name cannot be blank")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Instructor")
                }
            }
            "student" -> {
                Text(
                    text = "Manage Students",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                when (val state = studentsState) {
                    is AppState.Loading -> CircularProgressIndicator()
                    is AppState.Success -> {
                        val students = state.data.sortedBy { student -> student.fullName }
                        if (students.isEmpty()) {
                            Text(
                                text = "No students available.",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LazyColumn {
                                items(students) { student ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = student.fullName,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = {
                                            navController.navigate("editStudent/${student.id}")
                                        }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit Student")
                                        }
                                        IconButton(onClick = {
                                            studentToDelete = student
                                            showDialog = true
                                        }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete Student")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is AppState.Error -> Text("Error: ${state.message}")
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = showStudentDialog,
                    onExpandedChange = { showStudentDialog = !showStudentDialog }
                ) {
                    TextField(
                        value = selectedInstructor?.fullName ?: "Select Instructor",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Instructor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStudentDialog) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showStudentDialog,
                        onDismissRequest = { showStudentDialog = false }
                    ) {
                        when (val state = instructorsState) {
                            is AppState.Success -> {
                                state.data.forEach { instructor ->
                                    DropdownMenuItem(
                                        text = { Text(instructor.fullName) },
                                        onClick = {
                                            selectedInstructor = instructor
                                            showStudentDialog = false
                                        }
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = showProgramDialog,
                    onExpandedChange = { showProgramDialog = !showProgramDialog }
                ) {
                    TextField(
                        value = selectedProgram?.name ?: "Select Program",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Program") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProgramDialog) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showProgramDialog,
                        onDismissRequest = { showProgramDialog = false }
                    ) {
                        when (val state = programsState) {
                            is AppState.Success -> {
                                state.data.forEach { program ->
                                    DropdownMenuItem(
                                        text = { Text(program.name) },
                                        onClick = {
                                            selectedProgram = program
                                            showProgramDialog = false
                                        }
                                    )
                                }
                            }
                            else -> {}
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = newFirstName,
                    onValueChange = { newFirstName = it },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newLastName,
                    onValueChange = { newLastName = it },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("Pin") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (newFirstName.isNotBlank() && newLastName.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    val student = PeclStudentEntity(
                                        firstName = newFirstName,
                                        lastName = newLastName,
                                        fullName = "$newLastName, $newFirstName",
                                        grade = "",
                                        pin = newPin.toIntOrNull() ?: 0
                                    )
                                    viewModel.insertPeclStudent(student)
                                    if (selectedInstructor != null && selectedProgram != null) {
                                        viewModel.insertAssignment(
                                            InstructorStudentAssignmentEntity(
                                                instructor_id = selectedInstructor!!.id,
                                                student_id = student.id,
                                                program_id = selectedProgram!!.id
                                            )
                                        )
                                    }
                                    newFirstName = ""
                                    newLastName = ""
                                    newPin = ""
                                    selectedInstructor = null
                                    selectedProgram = null
                                    snackbarHostState.showSnackbar("Student added successfully")
                                } catch (e: Exception) {
                                    Log.e("UpdateUsersScreen", "Error adding student: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error adding student: ${e.message}")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("First and last name cannot be blank")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Student")
                }
            }
            else -> {
                Text(
                    text = "Manage Users",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                when (val state = usersState) {
                    is AppState.Loading -> CircularProgressIndicator()
                    is AppState.Success -> {
                        LazyColumn {
                            items(state.data.sortedBy { user -> user.fullName }) { user ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = user.fullName,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        navController.navigate("editUser/${user.id}/${user.role}")
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit User")
                                    }
                                    IconButton(onClick = {
                                        userToDelete = user
                                        showDialog = true
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete User")
                                    }
                                }
                            }
                        }
                    }
                    is AppState.Error -> Text("Error: ${state.message}")
                }
            }
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete this ${if (role == "student") "student" else "user"}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    if (role == "student") {
                                        studentToDelete?.let { viewModel.deletePeclStudent(it) }
                                    } else {
                                        userToDelete?.let { viewModel.deleteUser(it) }
                                    }
                                    showDialog = false
                                    snackbarHostState.showSnackbar("Deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("UpdateUsersScreen", "Error deleting: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting: ${e.message}")
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
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
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