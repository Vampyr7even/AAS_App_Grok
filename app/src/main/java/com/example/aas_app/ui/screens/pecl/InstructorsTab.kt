package com.example.aas_app.ui.screens.pecl

import android.util.Log
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.State
import kotlinx.coroutines.launch

@Composable
fun InstructorsTab() {
    val viewModel = hiltViewModel<AdminViewModel>()
    val usersState by viewModel.usersState.observeAsState(State.Success(emptyList()))
    var showAddInstructorDialog by remember { mutableStateOf(false) }
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newGrade by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var showEditInstructorDialog by remember { mutableStateOf<UserEntity?>(null) }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var editGrade by remember { mutableStateOf("") }
    var editPin by remember { mutableStateOf("") }
    var showDeleteInstructorDialog by remember { mutableStateOf<UserEntity?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Log.d("InstructorsTab", "Loading instructors")
        try {
            viewModel.loadAllUsers()
        } catch (e: Exception) {
            Log.e("InstructorsTab", "Error loading instructors: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading instructors: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Instructors",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showAddInstructorDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Instructor")
            }
            Text("Add Instructor", modifier = Modifier.padding(start = 4.dp))
        }

        when (val state = usersState) {
            is State.Loading -> Text(text = "Loading...")
            is State.Success -> {
                if (state.data.isEmpty()) {
                    Text("No instructors have been entered in the database. Add instructors to begin.")
                } else {
                    LazyColumn {
                        items(state.data.filter { it.role == "instructor" }) { instructor ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "${instructor.fullName} (${instructor.grade})")
                                    if (instructor.pin != null) {
                                        Text(
                                            text = "PIN: ${instructor.pin}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Row {
                                    IconButton(onClick = {
                                        editFirstName = instructor.firstName
                                        editLastName = instructor.lastName
                                        editGrade = instructor.grade
                                        editPin = instructor.pin?.toString() ?: ""
                                        showEditInstructorDialog = instructor
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit Instructor")
                                    }
                                    IconButton(onClick = {
                                        showDeleteInstructorDialog = instructor
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete Instructor")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is State.Error -> Text("Error: ${state.message}")
        }

        if (showAddInstructorDialog) {
            AlertDialog(
                onDismissRequest = { showAddInstructorDialog = false },
                title = { Text("Add Instructor") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = newFirstName,
                            onValueChange = { newFirstName = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = newLastName,
                            onValueChange = { newLastName = it },
                            label = { Text("Last Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = newGrade,
                            onValueChange = { newGrade = it },
                            label = { Text("Grade") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = newPin,
                            onValueChange = { newPin = it },
                            label = { Text("PIN (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newFirstName.isNotBlank() && newLastName.isNotBlank() && newGrade.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        val fullName = "$newLastName, $newFirstName"
                                        val user = UserEntity(
                                            firstName = newFirstName,
                                            lastName = newLastName,
                                            fullName = fullName,
                                            grade = newGrade,
                                            pin = newPin.toIntOrNull(),
                                            role = "instructor"
                                        )
                                        viewModel.insertUser(user)
                                        showAddInstructorDialog = false
                                        newFirstName = ""
                                        newLastName = ""
                                        newGrade = ""
                                        newPin = ""
                                        Toast.makeText(context, "Instructor added successfully", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("InstructorsTab", "Error adding instructor: ${e.message}", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Error adding instructor: ${e.message}")
                                        }
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("First name, last name, and grade are required")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp),
                        enabled = newFirstName.isNotBlank() && newLastName.isNotBlank() && newGrade.isNotBlank()
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

        showEditInstructorDialog?.let { instructor ->
            AlertDialog(
                onDismissRequest = { showEditInstructorDialog = null },
                title = { Text("Edit Instructor") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = editFirstName,
                            onValueChange = { editFirstName = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = editLastName,
                            onValueChange = { editLastName = it },
                            label = { Text("Last Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = editGrade,
                            onValueChange = { editGrade = it },
                            label = { Text("Grade") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = editPin,
                            onValueChange = { editPin = it },
                            label = { Text("PIN (optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editFirstName.isNotBlank() && editLastName.isNotBlank() && editGrade.isNotBlank()) {
                                coroutineScope.launch {
                                    try {
                                        val fullName = "$editLastName, $editFirstName"
                                        val updatedUser = instructor.copy(
                                            firstName = editFirstName,
                                            lastName = editLastName,
                                            fullName = fullName,
                                            grade = editGrade,
                                            pin = editPin.toIntOrNull(),
                                            role = "instructor"
                                        )
                                        viewModel.updateUser(updatedUser)
                                        showEditInstructorDialog = null
                                        editFirstName = ""
                                        editLastName = ""
                                        editGrade = ""
                                        editPin = ""
                                        Toast.makeText(context, "Instructor updated successfully", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("InstructorsTab", "Error updating instructor: ${e.message}", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Error updating instructor: ${e.message}")
                                        }
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("First name, last name, and grade are required")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp),
                        enabled = editFirstName.isNotBlank() && editLastName.isNotBlank() && editGrade.isNotBlank()
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showEditInstructorDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteInstructorDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteInstructorDialog = null },
                title = { Text("Confirm Delete") },
                text = { Text("Delete this instructor?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    showDeleteInstructorDialog?.let { viewModel.deleteUser(it) }
                                    showDeleteInstructorDialog = null
                                    Toast.makeText(context, "Instructor deleted successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("InstructorsTab", "Error deleting instructor: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting instructor: ${e.message}")
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
                        onClick = { showDeleteInstructorDialog = null },
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
}