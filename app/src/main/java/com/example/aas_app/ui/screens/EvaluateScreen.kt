package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluateScreen(navController: NavController) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val instructorsState by viewModel.instructorsState.observeAsState(AppState.Loading)
    val programsForInstructorState by viewModel.programsForInstructorState.observeAsState(AppState.Loading)
    val poisState by viewModel.poisState.observeAsState(AppState.Loading)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedInstructor by remember { mutableStateOf<UserEntity?>(null) }
    var selectedProgram by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var selectedPoi by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var expandedInstructor by remember { mutableStateOf(false) }
    var expandedProgram by remember { mutableStateOf(false) }
    var expandedPoi by remember { mutableStateOf(false) }
    var showProgramDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            viewModel.loadInstructors()
        } catch (e: Exception) {
            Log.e("EvaluateScreen", "Error loading instructors: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading instructors: ${e.message}")
            }
        }
    }

    LaunchedEffect(selectedInstructor) {
        selectedInstructor?.let { instructor ->
            try {
                viewModel.loadProgramsForInstructor(instructor.id)
            } catch (e: Exception) {
                Log.e("EvaluateScreen", "Error loading programs for instructor ${instructor.id}: ${e.message}", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading programs: ${e.message}")
                }
            }
        }
    }

    LaunchedEffect(programsForInstructorState) {
        when (val state = programsForInstructorState) {
            is AppState.Success -> {
                val programs = state.data
                when (programs.size) {
                    0 -> {
                        Log.w("EvaluateScreen", "No programs assigned for instructor")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Instructor has no assigned program")
                        }
                        selectedProgram = null
                        selectedPoi = null
                        showProgramDropdown = false
                    }
                    1 -> {
                        selectedProgram = programs.first()
                        selectedPoi = null
                        showProgramDropdown = false
                        selectedProgram?.let { program ->
                            try {
                                viewModel.loadPoisForProgram(program.id)
                            } catch (e: Exception) {
                                Log.e("EvaluateScreen", "Error loading POIs for program ${program.id}: ${e.message}", e)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error loading POIs: ${e.message}")
                                }
                            }
                        }
                    }
                    else -> {
                        selectedProgram = null
                        selectedPoi = null
                        showProgramDropdown = true
                    }
                }
            }
            is AppState.Error -> {
                Log.w("EvaluateScreen", "Programs load error: ${state.message}")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading programs: ${state.message}")
                }
            }
            is AppState.Loading -> {}
        }
    }

    LaunchedEffect(selectedProgram) {
        selectedProgram?.let { program ->
            try {
                viewModel.loadPoisForProgram(program.id)
            } catch (e: Exception) {
                Log.e("EvaluateScreen", "Error loading POIs for program ${program.id}: ${e.message}", e)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Error loading POIs: ${e.message}")
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        // Instructor Dropdown
        ExposedDropdownMenuBox(
            expanded = expandedInstructor,
            onExpandedChange = { expandedInstructor = !expandedInstructor }
        ) {
            TextField(
                readOnly = true,
                value = selectedInstructor?.fullName ?: "Select Instructor",
                onValueChange = { },
                label = { Text("Select Instructor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInstructor) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(
                    focusedContainerColor = Color(0xFFE57373),
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFFE57373),
                    unfocusedIndicatorColor = Color.Black
                ),
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedInstructor,
                onDismissRequest = { expandedInstructor = false }
            ) {
                when (val state = instructorsState) {
                    is AppState.Loading -> DropdownMenuItem(
                        text = { Text("Loading...") },
                        onClick = {}
                    )
                    is AppState.Success -> state.data.forEach { instructor ->
                        DropdownMenuItem(
                            text = { Text(instructor.fullName) },
                            onClick = {
                                selectedInstructor = instructor
                                expandedInstructor = false
                                // Reset downstream selections
                                selectedProgram = null
                                selectedPoi = null
                                showProgramDropdown = false
                            }
                        )
                    }
                    is AppState.Error -> DropdownMenuItem(
                        text = { Text("Error: ${state.message}") },
                        onClick = {}
                    )
                }
            }
        }

        // Program Dropdown (only if multiple programs assigned)
        if (showProgramDropdown) {
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expandedProgram,
                onExpandedChange = { expandedProgram = !expandedProgram }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedProgram?.name ?: "Select Program",
                    onValueChange = { },
                    label = { Text("Select Program") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProgram) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedContainerColor = Color(0xFFE57373),
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFFE57373),
                        unfocusedIndicatorColor = Color.Black
                    ),
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedProgram,
                    onDismissRequest = { expandedProgram = false }
                ) {
                    when (val state = programsForInstructorState) {
                        is AppState.Loading -> DropdownMenuItem(
                            text = { Text("Loading...") },
                            onClick = {}
                        )
                        is AppState.Success -> state.data.forEach { program ->
                            DropdownMenuItem(
                                text = { Text(program.name) },
                                onClick = {
                                    selectedProgram = program
                                    expandedProgram = false
                                    selectedPoi = null
                                }
                            )
                        }
                        is AppState.Error -> DropdownMenuItem(
                            text = { Text("Error: ${state.message}") },
                            onClick = {}
                        )
                    }
                }
            }
        }

        // POI Dropdown (shown if program is selected)
        if (selectedProgram != null) {
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expandedPoi,
                onExpandedChange = { expandedPoi = !expandedPoi }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedPoi?.name ?: "Select POI",
                    onValueChange = { },
                    label = { Text("Select POI") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPoi) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(
                        focusedContainerColor = Color(0xFFE57373),
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFFE57373),
                        unfocusedIndicatorColor = Color.Black
                    ),
                    modifier = Modifier.menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedPoi,
                    onDismissRequest = { expandedPoi = false }
                ) {
                    when (val state = poisState) {
                        is AppState.Loading -> DropdownMenuItem(
                            text = { Text("Loading...") },
                            onClick = {}
                        )
                        is AppState.Success -> state.data.forEach { poi ->
                            DropdownMenuItem(
                                text = { Text(poi.name) },
                                onClick = {
                                    selectedPoi = poi
                                    expandedPoi = false
                                }
                            )
                        }
                        is AppState.Error -> DropdownMenuItem(
                            text = { Text("Error: ${state.message}") },
                            onClick = {}
                        )
                    }
                }
            }
        }

        // Go to Dashboard Button (enabled if POI selected)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                try {
                    selectedProgram?.let { program ->
                        selectedPoi?.let { poi ->
                            selectedInstructor?.let { instructor ->
                                Log.d("EvaluateScreen", "Navigating to pecl/dashboard/${program.id}/${poi.id}/${instructor.id}")
                                navController.navigate("pecl/dashboard/${program.id}/${poi.id}/${instructor.id}")
                            } ?: run {
                                Log.w("EvaluateScreen", "Navigation failed: Instructor not selected")
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please select an instructor")
                                }
                            }
                        } ?: run {
                            Log.w("EvaluateScreen", "Navigation failed: POI not selected")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Please select a POI")
                            }
                        }
                    } ?: run {
                        Log.w("EvaluateScreen", "Navigation failed: Program not selected")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Please select a program")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("EvaluateScreen", "Navigation error to dashboard: ${e.message}", e)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Navigation failed: ${e.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            enabled = selectedProgram != null && selectedPoi != null && selectedInstructor != null
        ) {
            Text("Go to Dashboard")
        }

        // Snackbar for errors
        SnackbarHost(hostState = snackbarHostState)
    }
}