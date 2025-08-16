package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.DemographicsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUsersScreen(navController: NavController, role: String? = null) {
    val viewModel = hiltViewModel<DemographicsViewModel>()

    LaunchedEffect(Unit) {
        if (role == "instructor") {
            viewModel.loadInstructors()
        } else if (role == "student") {
            viewModel.loadStudents()
        } else {
            viewModel.loadUsers()
        }
    }

    val users by when (role) {
        "instructor" -> viewModel.instructors.observeAsState(emptyList())
        "student" -> viewModel.students.observeAsState(emptyList())
        else -> viewModel.users.observeAsState(emptyList())
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    var editUser by remember { mutableStateOf<UserEntity?>(null) }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var editGrade by remember { mutableStateOf("") }
    var editPin by remember { mutableStateOf<Int?>(null) }
    var editRole by remember { mutableStateOf(role ?: "") }
    var newFirstName by remember { mutableStateOf("") }
    var newLastName by remember { mutableStateOf("") }
    var newGrade by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf<Int?>(null) }
    var newRole by remember { mutableStateOf(role ?: "") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            items(users) { user ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = user.fullName, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        editUser = user
                        editFirstName = user.firstName
                        editLastName = user.lastName
                        editGrade = user.grade
                        editPin = user.pin
                        editRole = user.role ?: ""
                    }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { selectedUser = user; showDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        }

        TextField(
            value = newFirstName,
            onValueChange = { newFirstName = it },
            label = { Text("First Name") }
        )
        TextField(
            value = newLastName,
            onValueChange = { newLastName = it },
            label = { Text("Last Name") }
        )
        TextField(
            value = newGrade,
            onValueChange = { newGrade = it },
            label = { Text("Grade") }
        )
        TextField(
            value = newPin?.toString() ?: "",
            onValueChange = { newPin = it.toIntOrNull() },
            label = { Text("PIN") }
        )
        if (role == null) {
            TextField(
                value = newRole,
                onValueChange = { newRole = it },
                label = { Text("Role") }
            )
        }
        Button(
            onClick = {
                val fullName = "$newLastName, $newFirstName"
                viewModel.insertUser(UserEntity(firstName = newFirstName, lastName = newLastName, grade = newGrade, pin = newPin, fullName = fullName, role = newRole))
                newFirstName = ""
                newLastName = ""
                newGrade = ""
                newPin = null
                newRole = role ?: ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add User")
        }

        editUser?.let { user ->
            AlertDialog(
                onDismissRequest = { editUser = null },
                title = { Text("Edit User") },
                text = {
                    Column {
                        TextField(
                            value = editFirstName,
                            onValueChange = { editFirstName = it },
                            label = { Text("First Name") }
                        )
                        TextField(
                            value = editLastName,
                            onValueChange = { editLastName = it },
                            label = { Text("Last Name") }
                        )
                        TextField(
                            value = editGrade,
                            onValueChange = { editGrade = it },
                            label = { Text("Grade") }
                        )
                        TextField(
                            value = editPin?.toString() ?: "",
                            onValueChange = { editPin = it.toIntOrNull() },
                            label = { Text("PIN") }
                        )
                        if (role == null) {
                            TextField(
                                value = editRole,
                                onValueChange = { editRole = it },
                                label = { Text("Role") }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val fullName = "$editLastName, $editFirstName"
                        viewModel.updateUser(user.copy(firstName = editFirstName, lastName = editLastName, grade = editGrade, pin = editPin, fullName = fullName, role = editRole))
                        editUser = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { editUser = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this user?") },
            confirmButton = {
                Button(onClick = {
                    selectedUser?.let { viewModel.deleteUser(it) }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}