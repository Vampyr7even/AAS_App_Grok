package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.QuestionWithTask
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.PeclViewModel
import com.example.aas_app.viewmodel.State
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    navController: NavController,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val adminViewModel = hiltViewModel<AdminViewModel>()
    val peclViewModel = hiltViewModel<PeclViewModel>()
    val poisState by adminViewModel.poisState.observeAsState(State.Success(emptyList()))
    val studentsState by peclViewModel.studentsState.observeAsState(State.Success(emptyList()))
    val questionsState by peclViewModel.questionsState.observeAsState(State.Success(emptyList()))
    var selectedPoi by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var selectedStudent by remember { mutableStateOf<PeclStudentEntity?>(null) }
    var expandedPoi by remember { mutableStateOf(false) }
    var expandedStudent by remember { mutableStateOf(false) }
    val responses = remember { mutableStateMapOf<Long, String>() } // Store question responses

    LaunchedEffect(Unit) {
        adminViewModel.loadAllPois()
        peclViewModel.loadStudents()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedPoi,
                onDismissRequest = { expandedPoi = false }
            ) {
                when (val state = poisState) {
                    is State.Loading -> DropdownMenuItem(
                        text = { Text("Loading...") },
                        onClick = {}
                    )
                    is State.Success<List<PeclPoiEntity>> -> {
                        state.data.forEach { poi ->
                            DropdownMenuItem(
                                text = { Text(poi.name) },
                                onClick = {
                                    selectedPoi = poi
                                    expandedPoi = false
                                    peclViewModel.loadQuestionsForPoi(poi.id)
                                }
                            )
                        }
                    }
                    is State.Error -> DropdownMenuItem(
                        text = { Text("Error: ${state.message}") },
                        onClick = {}
                    )
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
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandedStudent,
                onDismissRequest = { expandedStudent = false }
            ) {
                when (val state = studentsState) {
                    is State.Loading -> DropdownMenuItem(
                        text = { Text("Loading...") },
                        onClick = {}
                    )
                    is State.Success<List<PeclStudentEntity>> -> {
                        state.data.forEach { student ->
                            DropdownMenuItem(
                                text = { Text(student.fullName) },
                                onClick = {
                                    selectedStudent = student
                                    expandedStudent = false
                                }
                            )
                        }
                    }
                    is State.Error -> DropdownMenuItem(
                        text = { Text("Error: ${state.message}") },
                        onClick = {}
                    )
                }
            }
        }

        when (val state = questionsState) {
            is State.Loading -> Text(
                text = "Loading questions...",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            is State.Success<List<QuestionWithTask>> -> {
                LazyColumn {
                    items(state.data) { questionWithTask ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = questionWithTask.question.subTask,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            when (questionWithTask.question.controlType) {
                                "TextBox" -> {
                                    TextField(
                                        value = responses[questionWithTask.question.id] ?: "",
                                        onValueChange = { responses[questionWithTask.question.id] = it },
                                        label = { Text("Enter response") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                "ComboBox" -> {
                                    var expanded by remember { mutableStateOf(false) }
                                    var selectedOption by remember { mutableStateOf("") }
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = !expanded }
                                    ) {
                                        TextField(
                                            readOnly = true,
                                            value = selectedOption,
                                            onValueChange = { },
                                            label = { Text("Select option") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            questionWithTask.question.scale.split(",").forEach { option ->
                                                DropdownMenuItem(
                                                    text = { Text(option.trim()) },
                                                    onClick = {
                                                        selectedOption = option.trim()
                                                        responses[questionWithTask.question.id] = selectedOption
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                // Add other control types as needed
                            }
                        }
                    }
                }
            }
            is State.Error -> Text(
                text = "Error: ${state.message}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = {
                if (selectedStudent != null && selectedPoi != null && responses.isNotEmpty()) {
                    coroutineScope.launch {
                        try {
                            responses.forEach { (questionId, response) ->
                                val result = PeclEvaluationResultEntity(
                                    student_id = selectedStudent!!.id,
                                    instructor_id = 1L, // Replace with actual instructor ID
                                    question_id = questionId,
                                    score = response.toDoubleOrNull() ?: 0.0, // Parse score for numeric responses
                                    comment = if (response.toDoubleOrNull() == null) response else "",
                                    timestamp = System.currentTimeMillis()
                                )
                                peclViewModel.insertEvaluationResult(result)
                            }
                            snackbarHostState.showSnackbar("Responses saved successfully")
                            responses.clear()
                            selectedStudent = null
                            selectedPoi = null
                        } catch (e: Exception) {
                            Log.e("SurveyScreen", "Error saving responses: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Please select a student, POI, and provide responses")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Responses")
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}