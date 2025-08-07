package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entities.PeclQuestionEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditQuestionScreen(navController: NavController, questionId: Long) {
    val viewModel: AdminViewModel = hiltViewModel()
    val questionState by viewModel.questionsState.observeAsState(AppState.Loading<List<PeclQuestionEntity>>())

    LaunchedEffect(questionId) {
        viewModel.loadQuestionById(questionId) // Assume added to ViewModel/Repo: getQuestionById
    }

    var subTask by remember { mutableStateOf("") }
    var controlType by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var criticalTask by remember { mutableStateOf("") }

    when (val state = questionState) {
        is AppState.Success -> {
            val question = state.data.firstOrNull { it.id == questionId } ?: return
            subTask = question.subTask
            controlType = question.controlType
            scale = question.scale
            criticalTask = question.criticalTask
        }
        else -> { }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = subTask,
            onValueChange = { subTask = it },
            label = { Text("Sub Task") }
        )
        TextField(
            value = controlType,
            onValueChange = { controlType = it },
            label = { Text("Control Type") }
        )
        TextField(
            value = scale,
            onValueChange = { scale = it },
            label = { Text("Scale") }
        )
        TextField(
            value = criticalTask,
            onValueChange = { criticalTask = it },
            label = { Text("Critical Task") }
        )
        Button(
            onClick = {
                val updatedQuestion = PeclQuestionEntity(questionId, subTask, controlType, scale, criticalTask)
                viewModel.updateQuestion(updatedQuestion)
                navController.popBackStack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Update")
        }
    }
}