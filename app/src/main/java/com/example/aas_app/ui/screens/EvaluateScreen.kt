package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluateScreen(navController: NavController) {
    val viewModel: PeclViewModel = hiltViewModel()
    val instructorsState by viewModel.instructorsState.observeAsState(AppState.Loading)
    val programsState by viewModel.programsForInstructorState.observeAsState(AppState.Loading)
    val poisState by viewModel.poisState.observeAsState(AppState.Loading)

    var selectedInstructor by remember { mutableStateOf<UserEntity?>(null) }
    var selectedProgram by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var selectedPoi by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var expandedInstructor by remember { mutableStateOf(false) }
    var expandedProgram by remember { mutableStateOf(false) }
    var expandedPoi by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInstructors()
    }

    LaunchedEffect(selectedInstructor) {
        selectedInstructor?.let {
            viewModel.loadProgramsForInstructor(it.id)
            selectedProgram = null
            selectedPoi = null
        }
    }

    LaunchedEffect(selectedProgram) {
        selectedProgram?.let {
            viewModel.loadPoisForProgram(it.id)
            selectedPoi = null
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Instructor Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedInstructor,
            onExpandedChange = { expandedInstructor = !expandedInstructor }
        ) {
            TextField(
                readOnly = true,
                value = selectedInstructor?.fullName ?: "",
                onValueChange = { },
                label = { Text("Select Instructor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstructor) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedInstructor,
                onDismissRequest = { expandedInstructor = false }
            ) {
                when (val state = instructorsState) {
                    is AppState.Loading -> DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                    is AppState.Success -> state.data.forEach { instructor ->
                        DropdownMenuItem(
                            text = { Text(instructor.fullName) },
                            onClick = {
                                selectedInstructor = instructor
                                expandedInstructor = false
                            }
                        )
                    }
                    is AppState.Error -> DropdownMenuItem(text = { Text("Error: ${state.message}") }, onClick = {})
                }
            }
        }

        // Program Dropdown (enabled after instructor selected)
        if (selectedInstructor != null) {
            ExposedDropdownMenuBox(
                expanded = expandedProgram,
                onExpandedChange = { expandedProgram = !expandedProgram }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedProgram?.name ?: "",
                    onValueChange = { },
                    label = { Text("Select Program") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgram) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedProgram,
                    onDismissRequest = { expandedProgram = false }
                ) {
                    when (val state = programsState) {
                        is AppState.Loading -> DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                        is AppState.Success -> state.data.forEach { program ->
                            DropdownMenuItem(
                                text = { Text(program.name) },
                                onClick = {
                                    selectedProgram = program
                                    expandedProgram = false
                                }
                            )
                        }
                        is AppState.Error -> DropdownMenuItem(text = { Text("Error: ${state.message}") }, onClick = {})
                    }
                }
            }
        }

        // POI Dropdown (enabled after program selected)
        if (selectedProgram != null) {
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
                        is AppState.Loading -> DropdownMenuItem(text = { Text("Loading...") }, onClick = {})
                        is AppState.Success -> state.data.forEach { poi ->
                            DropdownMenuItem(
                                text = { Text(poi.name) },
                                onClick = {
                                    selectedPoi = poi
                                    expandedPoi = false
                                }
                            )
                        }
                        is AppState.Error -> DropdownMenuItem(text = { Text("Error: ${state.message}") }, onClick = {})
                    }
                }
            }
        }

        // Navigate to Dashboard Button
        if (selectedProgram != null && selectedPoi != null) {
            Button(
                onClick = { navController.navigate("pecl/dashboard/${selectedProgram!!.id}/${selectedPoi!!.id}") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Go to Dashboard")
            }
        }
    }
}