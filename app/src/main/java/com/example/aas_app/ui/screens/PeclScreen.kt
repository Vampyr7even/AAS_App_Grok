package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.ui.screens.pecl.StudentsTab
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeclScreen(
    navController: NavController,
    instructorId: Long
) {
    val viewModel = hiltViewModel<PeclViewModel>()
    val programsState by viewModel.programsForInstructorState.observeAsState(AppState.Success(emptyList()))
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for selected program
    var selectedProgramId by remember { mutableStateOf<Long?>(null) }

    // Load programs for instructor
    LaunchedEffect(Unit) {
        try {
            viewModel.loadProgramsForInstructor(instructorId)
        } catch (e: Exception) {
            Log.e("PeclScreen", "Error loading programs: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading programs: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PECL Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = programsState) {
            is AppState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is AppState.Success -> {
                if (state.data.isEmpty()) {
                    Text(text = "No programs assigned to this instructor.")
                } else {
                    // Program selection dropdown
                    var expanded by remember { mutableStateOf(false) }
                    var selectedProgramName by remember { mutableStateOf("Select a program") }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        TextField(
                            readOnly = true,
                            value = selectedProgramName,
                            onValueChange = { },
                            label = { Text("Program") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            state.data.forEach { program ->
                                DropdownMenuItem(
                                    text = { Text(program.name) },
                                    onClick = {
                                        selectedProgramId = program.id
                                        selectedProgramName = program.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display StudentsTab when a program is selected
                    selectedProgramId?.let { programId ->
                        StudentsTab(
                            navController = navController,
                            instructorId = instructorId,
                            programId = programId
                        )
                    } ?: Text(text = "Please select a program to view students.")
                }
            }
            is AppState.Error -> Text(
                text = "Error: ${state.message}",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("addProgram") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Add Program")
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}