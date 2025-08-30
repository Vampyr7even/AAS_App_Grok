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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.AppResult
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(navController: NavController) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val poisState by viewModel.poisState.observeAsState(AppState.Loading)
    val studentsState by viewModel.studentsState.observeAsState(AppState.Loading)
    val questionsState by viewModel.questionsState.observeAsState(AppState.Loading)
    val coroutineScope = rememberCoroutineScope()

    var selectedPoi by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var selectedStudent by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var expandedPoi by remember { mutableStateOf(false) }
    var expandedStudent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadPois()
        viewModel.loadStudents()
    }

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
                label = { Text(text = "Select POI") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPoi) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedPoi,
                onDismissRequest = { expandedPoi = false }
            ) {
                when (val state = poisState) {
                    is AppState.Loading -> DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                    is AppState.Success -> {
                        state.data.forEach { poi ->
                            DropdownMenuItem(
                                text = { Text(text = poi.name) },
                                onClick = {
                                    selectedPoi = poi
                                    expandedPoi = false
                                    viewModel.loadQuestionsForPoi(poi.id)
                                }
                            )
                        }
                    }
                    is AppState.Error -> DropdownMenuItem(text = { Text("Error: ${state.message}") }, onClick = {})
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
                label = { Text(text = "Select Student") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStudent) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedStudent,
                onDismissRequest = { expandedStudent = false }
            ) {
                when (val state = studentsState) {
                    is AppState.Loading -> DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                    is AppState.Success -> {
                        state.data.forEach { student ->
                            DropdownMenuItem(
                                text = { Text(text = student.fullName) },
                                onClick = {
                                    selectedStudent = student
                                    expandedStudent = false
                                }
                            )
                        }
                    }
                    is AppState.Error -> DropdownMenuItem(text = { Text("Error: ${state.message}") }, onClick = {})
                }
            }
        }

        when (val state = questionsState) {
            is AppState.Loading -> Text(text = "Loading questions...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { question ->
                        Text(text = question.subTask)
                        // Add input field based on type
                    }
                }
            }
            is AppState.Error -> Text(text = "Error: ${state.message}")
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    selectedStudent?.let { student ->
                        selectedPoi?.let { poi ->
                            val result = PeclEvaluationResultEntity(
                                student_id = student.id,
                                instructor_id = 1L, // Replace with actual instructor ID
                                question_id = 0L, // Replace with actual question ID
                                score = 0.0, // Collect actual score
                                comment = "comment", // Collect actual comment
                                timestamp = System.currentTimeMillis()
                            )
                            viewModel.insertEvaluationResult(result)
                        }
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Save Responses")
        }
    }
}