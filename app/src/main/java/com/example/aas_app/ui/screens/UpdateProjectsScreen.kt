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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.util.Log
import android.widget.Toast
import com.example.aas_app.data.entity.ProjectEntity
import com.example.aas_app.viewmodel.AdminViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun UpdateProjectsScreen(viewModel: AdminViewModel) {
    val projects by viewModel.projects.collectAsState(initial = emptyList())
    var selectedProject by remember { mutableStateOf<ProjectEntity?>(null) }
    var projectName by remember { mutableStateOf(selectedProject?.projectName ?: "") }
    val context = LocalContext.current
    var addTriggered by remember { mutableStateOf(false) }

    LaunchedEffect(addTriggered) {
        if (addTriggered) {
            try {
                val newProject = ProjectEntity(projectName = projectName)
                viewModel.insertProject(newProject)
                Toast.makeText(context, "Project added", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("UpdateProjects", "Failed to add project", e)
                Toast.makeText(context, "Failed to add project: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            addTriggered = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        // List or dropdown for projects
        projects.forEach { project: ProjectEntity ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(project.projectName, modifier = Modifier.weight(1f))
                IconButton(onClick = { /* Implement edit */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = { /* Implement delete with confirmation */ }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }

        TextField(
            value = projectName,
            onValueChange = { newValue -> projectName = newValue },
            label = { Text("Project Name") }
        )

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
            Text("Add Project")
        }
    }
}