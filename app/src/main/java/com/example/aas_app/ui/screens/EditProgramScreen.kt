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
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.PeclViewModel

@Composable
fun EditProgramScreen(viewModel: PeclViewModel, programId: Int) {
    var program by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var programName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(programId) {
        val result = viewModel.getPeclProgramById(programId)
        if (result is Result.Success) {
            program = result.data
            programName = result.data?.peclProgram ?: ""
        }
        isLoading = false
    }

    if (isLoading) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    if (program == null) {
        Text("Program not found", modifier = Modifier.padding(16.dp))
        return
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = programName, onValueChange = { newValue -> programName = newValue }, label = { Text("Program Name") })

        Button(onClick = {
            val updatedProgram = program!!.copy(peclProgram = programName)
            viewModel.updatePeclProgram(updatedProgram)
        }) {
            Text("Update Program")
        }
    }
}