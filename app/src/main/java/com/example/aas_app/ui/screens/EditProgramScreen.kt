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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProgramScreen(
    navController: NavController,
    programId: Long,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val programsState by viewModel.programsState.observeAsState(State.Success(emptyList()))
    var name by remember { mutableStateOf("") }

    LaunchedEffect(programId) {
        viewModel.loadPrograms()
    }

    val program = when (val state = programsState) {
        is State.Success -> state.data.find { it.id == programId }
        else -> null
    }

    LaunchedEffect(program) {
        program?.let { name = it.name }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = programsState) {
            is State.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is State.Success -> {
                Text(
                    text = "Edit Program",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Program Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            coroutineScope.launch {
                                try {
                                    viewModel.updateProgram(PeclProgramEntity(id = programId, name = name))
                                    snackbarHostState.showSnackbar("Program updated successfully")
                                    navController.popBackStack()
                                } catch (e: Exception) {
                                    Log.e("EditProgramScreen", "Error updating program: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error updating program: ${e.message}")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Program name is required")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
            is State.Error -> Text(
                text = "Error: ${state.message}",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}