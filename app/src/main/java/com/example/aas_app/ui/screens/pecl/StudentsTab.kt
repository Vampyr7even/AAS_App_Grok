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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun StudentsTab(peclViewModel: PeclViewModel, errorMessage: String?, snackbarHostState: SnackbarHostState, coroutineScope: CoroutineScope) {
    val context = LocalContext.current
    val studentsState by peclViewModel.studentsState.observeAsState(AppState.Success(emptyList<PeclStudentEntity>()))
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
    var showStudentDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            peclViewModel.loadStudents()
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading students: ${e.message}")
            }
        }
    }

    LaunchedEffect(editStudent) {
        editStudent?.let { student ->
            editStudentFirstName = student.firstName
            editStudentLastName = student.lastName
            editStudentGrade = student.grade
            editStudentPin = student.pin
        }
    }

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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = student.fullName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            var programName by remember { mutableStateOf("Loading...") }
                            LaunchedEffect(student.id) {
                                try {
                                    val assignment = peclViewModel.getAssignmentForStudent(student.id)
                                    val programId = assignment?.program_id
                                    val program = programId?.let { peclViewModel.getProgramById(it) }
                                    programName = program?.name ?: "No Program"
                                } catch (e: Exception) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Error loading program for student: ${e.message}")
                                    }
                                    programName = "Error"
                                }
                            }
                            Text(
                                text = "Program: $programName",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            IconButton(onClick = {
                                editStudent = student
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
        }
        is AppState.Error -> Text("Error: ${state.message}")
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
                        val fullName = "$newStudentLastName, $newStudentFirstName"
                        if (fullName.isNotBlank() && newStudentFirstName.isNotBlank() && newStudentLastName.isNotBlank()) {
                            val newStudent = PeclStudentEntity(firstName = newStudentFirstName, lastName = newStudentLastName, grade = newStudentGrade, pin = newStudentPin, fullName = fullName)
                            peclViewModel.insertPeclStudent(newStudent)
                            showAddStudentDialog = false
                            newStudentFirstName = ""
                            newStudentLastName = ""
                            newStudentGrade = ""
                            newStudentPin = null
                            Toast.makeText(context, "Student added successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("First name and last name are required")
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
                        if (fullName.isNotBlank() && editStudentFirstName.isNotBlank() && editStudentLastName.isNotBlank()) {
                            editStudent?.let { student ->
                                val updatedStudent = student.copy(firstName = editStudentFirstName, lastName = editStudentLastName, grade = editStudentGrade, pin = editStudentPin, fullName = fullName)
                                peclViewModel.updatePeclStudent(updatedStudent)
                                showEditStudentDialog = false
                                Toast.makeText(context, "Student updated successfully", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("First name and last name are required")
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