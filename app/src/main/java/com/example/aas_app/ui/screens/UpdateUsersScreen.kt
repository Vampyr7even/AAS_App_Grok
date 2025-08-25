package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.DemographicsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateUsersScreen(navController: NavController, role: String? = null) {
    val viewModel = hiltViewModel<DemographicsViewModel>()
    val users by viewModel.users.observeAsState(emptyList())
    val instructors by viewModel.instructors.observeAsState(emptyList())
    val students by viewModel.students.observeAsState(emptyList())
    val programs by viewModel.programs.observeAsState(emptyList())

    LaunchedEffect(role) {
        if (role == "instructor") {
            viewModel.loadInstructors()
            viewModel.loadStudents()
            viewModel.loadPrograms()
        } else if (role == "student") {
            viewModel.loadStudents()
        } else {
            viewModel.loadUsers()
        }
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
    var showAddInstructorDialog by remember { mutableStateOf(false) }
    var newInstructorName by remember { mutableStateOf("") }
    var selectedStudentsForAdd by remember { mutableStateOf(setOf<Long>()) }
    var selectedProgramForAdd by remember { mutableStateOf<Long?>(null) }
    var expandedProgramAdd by remember { mutableStateOf(false) }
    var showEditInstructorDialog by remember { mutableStateOf(false) }
    var editInstructorName by remember { mutableStateOf("") }
    var selectedStudentsForEdit by remember { mutableStateOf(setOf<Long>()) }
    var selectedProgramForEdit by remember { mutableStateOf<Long?>(null) }
    var expandedProgramEdit by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(editUser) {
        editUser?.let { user ->
            editFirstName = user.firstName
            editLastName = user.lastName
            editGrade = user.grade
            editPin = user.pin
            editRole = user.role ?: ""
            if (role == "instructor") {
                coroutineScope.launch {
                    val assignments = viewModel.getAssignmentsForInstructor(user.id).value ?: emptyList()
                    selectedStudentsForEdit = assignments.map { it.student_id }.toSet()
                    selectedProgramForEdit = assignments.firstOrNull()?.program_id
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (role == "instructor") {
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

            val sortedInstructors = instructors.sortedBy { it.fullName }
            if (sortedInstructors.isEmpty()) {
                Text("No Instructors have been entered in the database. Add Instructors to begin.")
            } else {
                LazyColumn {
                    items(sortedInstructors) { instructor ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = instructor.fullName, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                editUser = instructor
                                editInstructorName = instructor.fullName
                                showEditInstructorDialog = true
                            }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedUser = instructor; showDialog = true }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        } else {
            if (users.isEmpty()) {
                Text("Database is empty")
            } else {
                val sortedUsers = users.sortedBy { it.fullName }
                LazyColumn {
                    items(sortedUsers) { user ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = user.fullName, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                editUser = user
                            }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedUser = user; showDialog = true }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
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
                    val newUser = UserEntity(firstName = newFirstName, lastName = newLastName, grade = newGrade, pin = newPin, fullName = fullName, role = newRole)
                    viewModel.insertUser(newUser)
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
                        Text("Select Students:")
                        LazyColumn {
                            items(students) { student ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectedStudentsForAdd.contains(student.id),
                                        onCheckedChange = { checked ->
                                            selectedStudentsForAdd = if (checked) selectedStudentsForAdd + student.id else selectedStudentsForAdd - student.id
                                        }
                                    )
                                    Text(student.fullName)
                                }
                            }
                        }
                        ExposedDropdownMenuBox(
                            expanded = expandedProgramAdd,
                            onExpandedChange = { expandedProgramAdd = !expandedProgramAdd }
                        ) {
                            TextField(
                                readOnly = true,
                                value = programs.find { it.id == selectedProgramForAdd }?.name ?: "",
                                onValueChange = { },
                                label = { Text("Select Program") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgramAdd) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedProgramAdd,
                                onDismissRequest = { expandedProgramAdd = false }
                            ) {
                                programs.forEach { program ->
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
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val fullName = newInstructorName
                                val newUser = UserEntity(firstName = "", lastName = "", grade = "", pin = null, fullName = fullName, role = "instructor")
                                val instructorId = viewModel.insertUserSync(newUser)
                                selectedStudentsForAdd.forEach { studentId ->
                                    viewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructorId, student_id = studentId, program_id = selectedProgramForAdd))
                                }
                                showAddInstructorDialog = false
                                newInstructorName = ""
                                selectedStudentsForAdd = emptySet()
                                selectedProgramForAdd = null
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
                        Text("Select Students:")
                        LazyColumn {
                            items(students) { student ->
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
                        ExposedDropdownMenuBox(
                            expanded = expandedProgramEdit,
                            onExpandedChange = { expandedProgramEdit = !expandedProgramEdit }
                        ) {
                            TextField(
                                readOnly = true,
                                value = programs.find { it.id == selectedProgramForEdit }?.name ?: "",
                                onValueChange = { },
                                label = { Text("Select Program") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgramEdit) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedProgramEdit,
                                onDismissRequest = { expandedProgramEdit = false }
                            ) {
                                programs.forEach { program ->
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
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                editUser?.let { user ->
                                    val updatedUser = user.copy(fullName = editInstructorName)
                                    viewModel.updateUser(updatedUser)
                                    viewModel.deleteAssignmentsForInstructor(user.id)
                                    selectedStudentsForEdit.forEach { studentId ->
                                        viewModel.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = user.id, student_id = studentId, program_id = selectedProgramForEdit))
                                    }
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

        editUser?.let { user ->
            if (role != "instructor") {
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
                        Button(
                            onClick = {
                                val fullName = "$editLastName, $editFirstName"
                                viewModel.updateUser(user.copy(firstName = editFirstName, lastName = editLastName, grade = editGrade, pin = editPin, fullName = fullName, role = editRole))
                                editUser = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { editUser = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this user?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUser?.let { viewModel.deleteUser(it) }
                        showDialog = false
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("No")
                }
            }
        )
    }
}