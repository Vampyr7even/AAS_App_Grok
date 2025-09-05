package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionScreen(navController: NavController, questionId: Long, taskId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    var subTask by remember { mutableStateOf("") }
    var controlType by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var criticalTask by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(questionId) {
        val question = viewModel.getQuestionById(questionId)
        question?.let {
            subTask = it.subTask
            controlType = it.controlType
            scale = it.scale
            criticalTask = it.criticalTask
        } ?: run {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading question")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Question",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = subTask,
            onValueChange = { subTask = it },
            label = { Text("Sub Task") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = controlType,
            onValueChange = { controlType = it },
            label = { Text("Control Type") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = scale,
            onValueChange = { scale = it },
            label = { Text("Scale") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = criticalTask,
            onValueChange = { criticalTask = it },
            label = { Text("Critical Task") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (subTask.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            val updatedQuestion = PeclQuestionEntity(
                                id = questionId,
                                subTask = subTask,
                                controlType = controlType,
                                scale = scale,
                                criticalTask = criticalTask
                            )
                            viewModel.updateQuestion(updatedQuestion, taskId)
                            snackbarHostState.showSnackbar("Question updated successfully")
                            navController.popBackStack()
                        } catch (e: Exception) {
                            Log.e("EditQuestionScreen", "Error updating question: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error updating question: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Subtask cannot be blank")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Question")
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}