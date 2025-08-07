package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.util.Log
import android.widget.Toast
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AdminViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun UpdateUsersScreen(viewModel: AdminViewModel) {
    val users by viewModel.users.collectAsState(initial = emptyList())
    var selectedUser by remember { mutableStateOf<UserEntity?>(null) }
    var firstName by remember { mutableStateOf(selectedUser?.firstName ?: "") }
    var lastName by remember { mutableStateOf(selectedUser?.lastName ?: "") }
    var grade by remember { mutableStateOf(selectedUser?.grade ?: "") }
    var pin by remember { mutableStateOf(selectedUser?.pin ?: 0) }
    var fullName by remember { mutableStateOf(selectedUser?.fullName ?: "") }
    var assignedProject by remember { mutableStateOf(selectedUser?.assignedProject ?: "") }
    val context = LocalContext.current
    var addTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(addTriggered) {
        if (addTriggered) {
            try {
                val newUser = UserEntity(
                    firstName = firstName,
                    lastName = lastName,
                    grade = grade,
                    pin = pin,
                    fullName = fullName,
                    assignedProject = assignedProject
                )
                viewModel.insertUser(newUser)
                Toast.makeText(context, "User added", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("UpdateUsers", "Failed to add user", e)
                Toast.makeText(context, "Failed to add user: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            addTriggered = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        // List users
        users.forEach { user: UserEntity ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(user.fullName, modifier = Modifier.weight(1f))
                IconButton(onClick = { /* Implement edit */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { /* Implement delete with confirmation */ }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }

        TextField(
            value = firstName,
            onValueChange = { newValue -> firstName = newValue },
            label = { Text("First Name") }
        )
        TextField(
            value = lastName,
            onValueChange = { newValue -> lastName = newValue },
            label = { Text("Last Name") }
        )
        TextField(
            value = grade,
            onValueChange = { newValue -> grade = newValue },
            label = { Text("Grade") }
        )

        // Pin, fullName, assignedProject similarly

        Button(
            onClick = { addTriggered = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE57373),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add User")
        }
    }
}