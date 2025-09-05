package com.example.aas_app.ui.screens.pecl

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
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.DemographicsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorsTab(navController: NavController, demographicsViewModel: DemographicsViewModel, peclViewModel: com.example.aas_app.viewmodel.PeclViewModel, errorMessage: String?, snackbarHostState: SnackbarHostState, coroutineScope: CoroutineScope) {
    val viewModel = hiltViewModel<DemographicsViewModel>()
    val instructorsState by viewModel.instructorsState.observeAsState(AppState.Loading)
    val programsState by viewModel.programsState.observeAsState(AppState.Loading)
    val instructorAssignmentsState by viewModel.instructorAssignmentsState.observeAsState(AppState.Loading)
    val instructorProgramAssignmentsState by viewModel.instructorProgramAssignmentsState.observeAsState(AppState.Loading)
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var instructorToDelete by remember { mutableStateOf<UserEntity?>(null) }
    var selectedProgram by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var showProgramDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInstructors()
        viewModel.loadPrograms()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Manage Instructors",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = instructorsState) {
            is AppState.Loading -> CircularProgressIndicator()
            is AppState.Success -> {
                if (state.data.isEmpty()) {
                    Text(
                        text = "No instructors available.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LazyColumn {
                        items(state.data.sortedBy { instructor -> instructor.fullName }) { instructor ->
                            val assignments = remember { mutableStateOf<List<InstructorStudentAssignmentEntity>>(emptyList()) }
                            val programIds = remember { mutableStateOf<List<Long>>(emptyList()) }
                            val programsForInstructor = remember { mutableStateOf<List<PeclProgramEntity>>(emptyList()) }
                            coroutineScope.launch {
                                try {
                                    assignments.value = viewModel.getAssignmentsForInstructorSync(instructor.id)
                                    programIds.value = viewModel.getProgramIdsForInstructorSync(instructor.id)
                                    val programState = programsState
                                    if (programState is AppState.Success) {
                                        programsForInstructor.value = programState.data.filter { it.id in programIds.value }.sortedBy { program -> program.name }
                                    }
                                } catch (e: Exception) {
                                    Log.e("InstructorsTab", "Error loading assignments or programs: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error loading data: ${e.message}")
                                }
                            }
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
                                    text = programsForInstructor.value.joinToString { program -> program.name },
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
                                                instructorToDelete = instructor
                                                showDialog = true
                                            } else {
                                                snackbarHostState.showSnackbar("Cannot delete instructor with assigned students or programs")
                                            }
                                        } catch (e: Exception) {
                                            Log.e("InstructorsTab", "Error checking deletion: ${e.message}", e)
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
                                    pin = newPin.toIntOrNull(),
                                    role = "instructor"
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
                            Log.e("InstructorsTab", "Error adding instructor: ${e.message}", e)
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

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Delete") },
                text = { Text("Are you sure you want to delete this instructor?") },
                confirmButton = {
                    Button(
                        onClick = {
                            instructorToDelete?.let { instructor ->
                                coroutineScope.launch {
                                    try {
                                        viewModel.deleteInstructorProgramAssignmentsForInstructor(instructor.id)
                                        if (viewModel.canDeleteInstructor(instructor.id)) {
                                            viewModel.deleteUser(instructor)
                                        }
                                        showDialog = false
                                        snackbarHostState.showSnackbar("Instructor deleted successfully")
                                    } catch (e: Exception) {
                                        Log.e("InstructorsTab", "Error deleting instructor: ${e.message}", e)
                                        snackbarHostState.showSnackbar("Error deleting instructor: ${e.message}")
                                    }
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