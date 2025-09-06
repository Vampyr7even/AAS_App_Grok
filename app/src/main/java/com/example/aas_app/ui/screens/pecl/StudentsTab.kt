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
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsTab(
    navController: NavController,
    peclViewModel: PeclViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val context = LocalContext.current
    val studentsState by peclViewModel.studentsState.observeAsState(AppState.Success(emptyList()))
    val programsState by peclViewModel.programsForInstructorState.observeAsState(AppState.Success(emptyList()))
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var newStudentName by remember { mutableStateOf("") }
    var selectedProgramForAdd by remember { mutableStateOf<Long?>(null) }
    var showEditStudentDialog by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var editStudentName by remember { mutableStateOf("") }
    var selectedProgramForEdit by remember { mutableStateOf<Long?>(null) }
    var showDeleteDialog by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var expandedProgramAdd by remember { mutableStateOf(false) }
    var expandedProgramEdit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            peclViewModel.loadStudents()
            peclViewModel.loadProgramsForInstructor(0L)
        } catch (e: Exception) {
            Log.e("StudentsTab", "Error loading data: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading data: ${e.message}")
            }
        }
    }

    LaunchedEffect(showEditStudentDialog) {
        showEditStudentDialog?.let { student ->
            editStudentName = student.fullName
            selectedProgramForEdit = student.programId
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Students",
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
                if (state.data.isEmpty()) {
                    Text("No students available")
                } else {
                    LazyColumn {
                        items(state.data.sortedBy { it.fullName }) { student ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = student.fullName)
                                    Text(
                                        text = "Program: ${student.programId?.let { id ->
                                            programsState.let { progState ->
                                                if (progState is AppState.Success) {
                                                    progState.data.find { it.id == id }?.name ?: "None"
                                                } else {
                                                    "None"
                                                }
                                            }
                                        } ?: "None"}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { showEditStudentDialog = student }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit Student")
                                }
                                IconButton(onClick = { showDeleteDialog = student }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete Student")
                                }
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }
    }

    if (showAddStudentDialog) {
        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Add Student") },
            text = {
                Column {
                    TextField(
                        value = newStudentName,
                        onValueChange = { newStudentName = it },
                        label = { Text("Student Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedProgramAdd,
                        onExpandedChange = { expandedProgramAdd = !expandedProgramAdd }
                    ) {
                        TextField(
                            readOnly = true,
                            value = programsState.let { state ->
                                if (state is AppState.Success) {
                                    state.data.find { it.id == selectedProgramForAdd }?.name ?: "Select Program"
                                } else {
                                    "Select Program"
                                }
                            },
                            onValueChange = { },
                            label = { Text("Program") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgramAdd) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProgramAdd,
                            onDismissRequest = { expandedProgramAdd = false }
                        ) {
                            programsState.let { state ->
                                if (state is AppState.Success) {
                                    state.data.forEach { program ->
                                        DropdownMenuItem(
                                            text = { Text(program.name) },
                                            onClick = {
                                                selectedProgramForAdd = program.id
                                                expandedProgramAdd = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStudentName.isNotBlank() && selectedProgramForAdd != null) {
                            coroutineScope.launch {
                                try {
                                    peclViewModel.insertPeclStudent(
                                        PeclStudentEntity(
                                            id = 0L, // Auto-generated by DB
                                            fullName = newStudentName,
                                            programId = selectedProgramForAdd
                                        )
                                    )
                                    showAddStudentDialog = false
                                    newStudentName = ""
                                    selectedProgramForAdd = null
                                    Toast.makeText(context, "Student added", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("StudentsTab", "Error adding student: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error adding student: ${e.message}")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Name and program required")
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
                    onClick = { showAddStudentDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    showEditStudentDialog?.let { student ->
        AlertDialog(
            onDismissRequest = { showEditStudentDialog = null },
            title = { Text("Edit Student") },
            text = {
                Column {
                    TextField(
                        value = editStudentName,
                        onValueChange = { editStudentName = it },
                        label = { Text("Student Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedProgramEdit,
                        onExpandedChange = { expandedProgramEdit = !expandedProgramEdit }
                    ) {
                        TextField(
                            readOnly = true,
                            value = programsState.let { state ->
                                if (state is AppState.Success) {
                                    state.data.find { it.id == selectedProgramForEdit }?.name ?: "Select Program"
                                } else {
                                    "Select Program"
                                }
                            },
                            onValueChange = { },
                            label = { Text("Program") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgramEdit) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedProgramEdit,
                            onDismissRequest = { expandedProgramEdit = false }
                        ) {
                            programsState.let { state ->
                                if (state is AppState.Success) {
                                    state.data.forEach { program ->
                                        DropdownMenuItem(
                                            text = { Text(program.name) },
                                            onClick = {
                                                selectedProgramForEdit = program.id
                                                expandedProgramEdit = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editStudentName.isNotBlank() && selectedProgramForEdit != null) {
                            coroutineScope.launch {
                                try {
                                    peclViewModel.updatePeclStudent(
                                        student.copy(
                                            fullName = editStudentName,
                                            programId = selectedProgramForEdit
                                        )
                                    )
                                    showEditStudentDialog = null
                                    editStudentName = ""
                                    selectedProgramForEdit = null
                                    Toast.makeText(context, "Student updated", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("StudentsTab", "Error updating student: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error updating student: ${e.message}")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Name and program required")
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
                    onClick = { showEditStudentDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    showDeleteDialog?.let { student ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirm Delete") },
            text = { Text("Delete ${student.fullName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                peclViewModel.deletePeclStudent(student)
                                showDeleteDialog = null
                                Toast.makeText(context, "Student deleted", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("StudentsTab", "Error deleting student: ${e.message}", e)
                                snackbarHostState.showSnackbar("Error deleting student: ${e.message}")
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