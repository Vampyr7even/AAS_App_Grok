package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.aas_app.data.entity.DemoTemplatesEntity
import com.example.aas_app.data.entity.QuestionRepositoryEntity
import com.example.aas_app.viewmodel.DemographicsViewModel
import androidx.compose.foundation.lazy.items

@Composable
fun BuilderScreen(viewModel: DemographicsViewModel) {
    val questions by viewModel.questions.collectAsState(initial = emptyList<QuestionRepositoryEntity>())
    val templates by viewModel.demoTemplates.collectAsState(initial = emptyList<DemoTemplatesEntity>())
    var selectedQuestions by remember { mutableStateOf(listOf<Int>()) }
    var templateName by remember { mutableStateOf("") }
    var showAddTemplateDialog by remember { mutableStateOf(false) }
    var showEditTemplateDialog by remember { mutableStateOf<DemoTemplatesEntity?>(null) }
    var showDeleteTemplateDialog by remember { mutableStateOf<DemoTemplatesEntity?>(null) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Questions")
        LazyColumn {
            items(questions) { question ->
                Row {
                    Checkbox(
                        checked = selectedQuestions.contains(question.id),
                        onCheckedChange = { checked ->
                            selectedQuestions = if (checked) selectedQuestions + question.id else selectedQuestions - question.id
                        }
                    )
                    Text(question.field)
                }
            }
        }

        Button(onClick = { showAddTemplateDialog = true }) {
            Text("Save as Template")
        }

        Text("Templates")
        LazyColumn {
            items(templates) { template ->
                Row {
                    Text(template.templateName)
                    Button(onClick = { showEditTemplateDialog = template }) { Text("Edit") }
                    Button(onClick = { showDeleteTemplateDialog = template }) { Text("Delete") }
                }
            }
        }

        if (showAddTemplateDialog) {
            AlertDialog(
                onDismissRequest = { showAddTemplateDialog = false },
                title = { Text("Add Template") },
                text = {
                    TextField(value = templateName, onValueChange = { templateName = it }, label = { Text("Template Name") })
                },
                confirmButton = {
                    Button(onClick = {
                        val template = DemoTemplatesEntity(templateName = templateName, selectedItems = selectedQuestions.joinToString(","))
                        viewModel.insertTemplate(template)
                        showAddTemplateDialog = false
                    }) { Text("Save") }
                }
            )
        }

        showEditTemplateDialog?.let { template ->
            var newName by remember { mutableStateOf(template.templateName) }
            AlertDialog(
                onDismissRequest = { showEditTemplateDialog = null },
                title = { Text("Edit Template") },
                text = {
                    TextField(value = newName, onValueChange = { newName = it }, label = { Text("Template Name") })
                },
                confirmButton = {
                    Button(onClick = {
                        val updatedTemplate = template.copy(templateName = newName)
                        viewModel.updateDemoTemplate(updatedTemplate)
                        showEditTemplateDialog = null
                    }) { Text("Update") }
                }
            )
        }

        showDeleteTemplateDialog?.let { template ->
            AlertDialog(
                onDismissRequest = { showDeleteTemplateDialog = null },
                title = { Text("Delete Template") },
                text = { Text("Delete ${template.templateName}?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteDemoTemplate(template)
                        showDeleteTemplateDialog = null
                        Toast.makeText(context, "Template deleted", Toast.LENGTH_SHORT).show()
                    }) { Text("Delete") }
                }
            )
        }
    }
}