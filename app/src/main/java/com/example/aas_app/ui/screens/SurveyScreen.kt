package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
modifier = Modifier.fillMaxSize().padding(16.dp),
verticalArrangement = Arrangement.Center
) {
    ExposedDropdownMenuBox(
        expanded = expandedPoi,
        onExpandedChange = { expandedPoi = !expandedPoi }
    ) {
        TextField(
            readOnly = true,
            value = selectedPoi?.name ?: "",
            onValueChange = { },
            label = { Text("Select POI") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPoi) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expandedPoi,
            onDismissRequest = { expandedPoi = false }
        ) {
            when (val state = poisState) {
                is AppState.Success -> state.data.forEach { poi ->
                    DropdownMenuItem(
                        text = { Text(poi.name) },
                        onClick = {
                            selectedPoi = poi
                            expandedPoi = false
                            viewModel.loadQuestionsForTask(poi.id) // Adjust to load questions for POI
                        }
                    )
                }
                else -> { }
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expandedStudent,
        onExpandedChange = { expandedStudent = !expandedStudent }
    ) {
        TextField(
            readOnly = true,
            value = selectedStudent?.fullName ?: "",
            onValueChange = { },
            label = { Text("Select Student") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudent) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expandedStudent,
            onDismissRequest = { expandedStudent = false }
        ) {
            when (val state = studentsState) {
                is AppState.Success -> state.data.forEach { student ->
                    DropdownMenuItem(
                        text = { Text(student.fullName) },
                        onClick = {
                            selectedStudent = student
                            expandedStudent = false
                        }
                    )
                }
                else -> { }
            }
        }
    }

    when (val state = questionsState) {
        is AppState.Loading -> Text("Loading questions...")
        is AppState.Success -> {
            LazyColumn {
                items(state.data) { question ->
                    // Render dynamic control based on question.controlType
                    Text(question.subTask)
                    // Add input field based on type
                }
            }
        }
        is AppState.Error -> Text("Error: ${state.message}")
    }

    Button(
        onClick = {
            // Collect responses and insert
            val result = PeclEvaluationResultEntity(0, 0, 0, 0, 0.0, "comment", 0L)
            viewModel.insertEvaluationResult(result)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text("Save Responses")
    }
}
}