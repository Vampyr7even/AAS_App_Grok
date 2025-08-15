package com.example.aas_app.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.DemographicsViewModel

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

    val users = when (role) {
        "instructor" -> viewModel.instructors.value.orEmpty()
        "student" -> viewModel.students.value.orEmpty()
        else -> viewModel.users.value.orEmpty()
    }

    // List users...

    val userName = remember { mutableStateOf("") }
    val userRole = remember { mutableStateOf(role ?: "") }

    TextField(value = userName.value, onValueChange = { userName.value = it })
    TextField(value = userRole.value, onValueChange = { userRole.value = it })

    Button(onClick = {
        viewModel.insertUser(UserEntity(0, userName.value, "", "", null, userName.value, null, userRole.value))
    }) {
        Text("Add User")
    }
}