package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aas_app.data.entity.QuestionRepositoryEntity
import com.example.aas_app.viewmodel.DemographicsViewModel
import androidx.compose.foundation.lazy.items

@Composable
fun RepositoryScreen(viewModel: DemographicsViewModel) {
    val questions by viewModel.questions.collectAsState(initial = emptyList<QuestionRepositoryEntity>())
    var field by remember { mutableStateOf("") }
    var inputType by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<QuestionRepositoryEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf<QuestionRepositoryEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn {
            items(questions) { question ->
                Row {
                    Text(question.field)
                    Text(question.inputType)
                    if (question.inputType == "ComboBox") Text(question.options)
                    Button(onClick = { showEditDialog = question }) { Text("Edit") }
                    Button(onClick = { showDeleteDialog = question }) { Text("Delete") }
                }
            }
        }

        Button(onClick = { showAddDialog = true }) { Text("Add Question") }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Question") },
                text = {
                    Column {
                        TextField(value = field, onValueChange = { field = it }, label = { Text("Field") })
                        TextField(value = inputType, onValueChange = { inputType = it }, label = { Text("Input Type") })
                        if (inputType == "ComboBox") TextField(value = options, onValueChange = { options = it }, label = { Text("Options") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val question = QuestionRepositoryEntity(field = field, inputType = inputType, options = options)
                        viewModel.insertQuestion(question)
                        showAddDialog = false
                    }) { Text("Add") }
                }
            )
        }

        showEditDialog?.let { question ->
            var newField by remember { mutableStateOf(question.field) }
            var newInputType by remember { mutableStateOf(question.inputType) }
            var newOptions by remember { mutableStateOf(question.options) }
            AlertDialog(
                onDismissRequest = { showEditDialog = null },
                title = { Text("Edit Question") },
                text = {
                    Column {
                        TextField(value = newField, onValueChange = { newField = it }, label = { Text("Field") })
                        TextField(value = newInputType, onValueChange = { newInputType = it }, label = { Text("Input Type") })
                        if (newInputType == "ComboBox") TextField(value = newOptions, onValueChange = { newOptions = it }, label = { Text("Options") })
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val updatedQuestion = question.copy(field = newField, inputType = newInputType, options = newOptions)
                        viewModel.updateQuestion(updatedQuestion)
                        showEditDialog = null
                    }) { Text("Update") }
                }
            )
        }

        showDeleteDialog?.let { question ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Delete Question") },
                text = { Text("Delete ${question.field}?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteQuestion(question)
                        showDeleteDialog = null
                    }) { Text("Delete") }
                }
            )
        }
    }
}