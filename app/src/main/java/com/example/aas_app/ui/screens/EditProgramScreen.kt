package com.example.aas_app.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState

@Composable
fun EditProgramScreen(adminViewModel: AdminViewModel, programId: Long) {
    LaunchedEffect(programId) {
        adminViewModel.loadPrograms()
    }

    val program = adminViewModel.programsState.value?.let { state ->
        if (state is AppState.Success<List<PeclProgramEntity>>) {
            state.data.find { it.id == programId }
        } else null
    }

    val name = remember { mutableStateOf(program?.name ?: "") }

    TextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Program Name") })

    Button(onClick = {
        adminViewModel.updateProgram(PeclProgramEntity(id = programId, name = name.value))
    }) {
        Text("Save")
    }
}