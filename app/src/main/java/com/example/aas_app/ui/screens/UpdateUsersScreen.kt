package com.example.aas_app.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.aas_app.data.UserEntity
import com.example.aas_app.viewmodel.DemographicsViewModel

@Composable
fun UpdateUsersScreen(viewModel: DemographicsViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val users = viewModel.users.value.orEmpty()

    // List users...

    val userName = mutableStateOf("")
    val userRole = mutableStateOf("")

    TextField(value = userName.value, onValueChange = { userName.value = it })
    TextField(value = userRole.value, onValueChange = { userRole.value = it })

    Button(onClick = {
        viewModel.insertUser(UserEntity(name = userName.value, role = userRole.value))
    }) {
        Text("Add User")
    }
}