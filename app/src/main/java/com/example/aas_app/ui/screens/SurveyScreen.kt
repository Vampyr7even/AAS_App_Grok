package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aas_app.data.entity.ResponseEntity
import com.example.aas_app.viewmodel.DemographicsViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(viewModel: DemographicsViewModel) {
    val templates by viewModel.demoTemplates.collectAsState(initial = emptyList())
    var selectedTemplate by remember { mutableStateOf("") }
    val selectedQuestions by viewModel.selectedQuestions.collectAsState(initial = emptyList())
    val users by viewModel.users.collectAsState(initial = emptyList())
    val responses = remember { mutableStateMapOf<Int, String>() }
    var selectedUser by remember { mutableStateOf("") }
    var expandedTemplate by remember { mutableStateOf(false) }
    var expandedUser by remember { mutableStateOf(false) }

    // Load for default project, adjust as needed
    viewModel.loadUsersByAssignedProject("AASB")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Template dropdown
        ExposedDropdownMenuBox(expanded = expandedTemplate, onExpandedChange = { expandedTemplate = !expandedTemplate }) {
            TextField(readOnly = true, value = selectedTemplate, onValueChange = {}, label = { Text("Template") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTemplate) })
            ExposedDropdownMenu(expanded = expandedTemplate, onDismissRequest = { expandedTemplate = false }) {
                templates.forEach { template ->
                    DropdownMenuItem(text = { Text(template.templateName) }, onClick = {
                        selectedTemplate = template.templateName
                        viewModel.loadQuestionsByIds(template.selectedItems.split(",").map { it.toInt() })
                        expandedTemplate = false
                    })
                }
            }
        }

        // User dropdown
        ExposedDropdownMenuBox(expanded = expandedUser, onExpandedChange = { expandedUser = !expandedUser }) {
            TextField(readOnly = true, value = selectedUser, onValueChange = {}, label = { Text("User") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUser) })
            ExposedDropdownMenu(expanded = expandedUser, onDismissRequest = { expandedUser = false }) {
                users.forEach { user ->
                    DropdownMenuItem(text = { Text(user.fullName) }, onClick = { selectedUser = user.fullName; expandedUser = false })
                }
            }
        }

        LazyColumn {
            items(selectedQuestions.size) { index ->
                val question = selectedQuestions[index]
                Column {
                    Text(question.field)
                    when (question.inputType) {
                        "TextBox" -> {
                            var text by remember { mutableStateOf(responses[question.id] ?: "") }
                            TextField(value = text, onValueChange = { text = it; responses[question.id] = it })
                        }
                        "ComboBox" -> {
                            var selected by remember { mutableStateOf(responses[question.id] ?: "") }
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                                TextField(readOnly = true, value = selected, onValueChange = {}, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) })
                                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    question.options.split(",").forEach { option ->
                                        DropdownMenuItem(text = { Text(option) }, onClick = { selected = option; responses[question.id] = option; expanded = false })
                                    }
                                }
                            }
                        }
                        // Add other types
                    }
                }
            }
        }

        Button(onClick = {
            responses.forEach { (questionId, response) ->
                viewModel.insertResponse(ResponseEntity(questionId = questionId, userId = users.find { it.fullName == selectedUser }?.id ?: 0, answer = response, surveyDate = Date().toString()))
            }
        }) {
            Text("Submit")
        }
    }
}