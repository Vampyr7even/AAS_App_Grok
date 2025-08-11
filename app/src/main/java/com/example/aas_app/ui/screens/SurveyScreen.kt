package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(navController: NavController) {
    val viewModel: AdminViewModel = hiltViewModel() // Adjust to appropriate ViewModel if needed
    val poisState by viewModel.poisState.observeAsState(AppState.Loading<List<PeclPoiEntity>>())
    val studentsState by viewModel.studentsState.observeAsState(AppState.Loading<List<UserEntity>>())
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading<List<PeclQuestionEntity>>())

    var selectedPoi by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var selectedStudent by remember { mutableStateOf<UserEntity?>(null) }
    var expandedPoi by remember { mutableStateOf(false) }
    var expandedStudent by remember { mutableStateOf(false) }

    Column(
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
                                viewModel.loadQuestionsForPoi(poi.id) // Adjust to load questions for POI
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