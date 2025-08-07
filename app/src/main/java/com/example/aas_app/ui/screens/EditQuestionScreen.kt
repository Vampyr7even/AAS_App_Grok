package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aas_app.data.Result
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.PeclViewModel

@Composable
fun EditQuestionScreen(viewModel: PeclViewModel, questionId: Int) {
    var question by remember { mutableStateOf<PeclQuestionEntity?>(null) }
    var subTask by remember { mutableStateOf("") }
    var controlType by remember { mutableStateOf("") }
    var scale by remember { mutableStateOf("") }
    var criticalTask by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(questionId) {
        val result = viewModel.getPeclQuestionById(questionId)
        if (result is Result.Success) {
            question = result.data
            subTask = result.data?.subTask ?: ""
            controlType = result.data?.controlType ?: ""
            scale = result.data?.scale ?: ""
            criticalTask = result.data?.criticalTask ?: ""
        }
        isLoading = false
    }

    if (isLoading) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    if (question == null) {
        Text("Question not found", modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = subTask, onValueChange = { newValue -> subTask = newValue }, label = { Text("Sub Task") })
        TextField(value = controlType, onValueChange = { newValue -> controlType = newValue }, label = { Text("Control Type") })
        TextField(value = scale, onValueChange = { newValue -> scale = newValue }, label = { Text("Scale") })
        TextField(value = criticalTask, onValueChange = { newValue -> criticalTask = newValue }, label = { Text("Critical Task") })

        Button(onClick = {
            val updatedQuestion = question!!.copy(subTask = subTask, controlType = controlType, scale = scale, criticalTask = criticalTask)
            viewModel.updatePeclQuestion(updatedQuestion)
        }) {
            Text("Update Question")
        }
    }
}