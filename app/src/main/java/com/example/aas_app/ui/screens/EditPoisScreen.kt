package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PoiWithPrograms

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPoisScreen(navController: NavController, programId: Long) {
    val viewModel: AdminViewModel = hiltViewModel()
    val poisState by viewModel.poisState.observeAsState(AppState.Loading as AppState<List<PoiWithPrograms>>)

    LaunchedEffect(programId) {
        viewModel.loadPoisForProgram(programId)
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedPoi by remember { mutableStateOf<PoiWithPrograms?>(null) }
    var editPoi by remember { mutableStateOf<PoiWithPrograms?>(null) }
    var editName by remember { mutableStateOf("") }
    var newPoiName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = poisState) {
            is AppState.Loading -> Text("Loading...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { poiWithPrograms ->
                        val poi = poiWithPrograms.poi
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(poi.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = { editPoi = poiWithPrograms; editName = poi.name }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedPoi = poiWithPrograms; showDialog = true }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
            is AppState.Error -> Text("Error: ${state.message}")
        }

        TextField(
            value = newPoiName,
            onValueChange = { newPoiName = it },
            label = { Text("New POI Name") }
        )
        Button(
            onClick = {
                viewModel.insertPoi(PeclPoiEntity(name = newPoiName), listOf(programId))
                newPoiName = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add POI")
        }

        editPoi?.let { poiWithPrograms ->
            AlertDialog(
                onDismissRequest = { editPoi = null },
                title = { Text("Edit POI") },
                text = {
                    TextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("POI Name") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        viewModel.updatePoi(poiWithPrograms.poi.copy(name = editName), listOf(programId)) // Adjust with new programIds if multi
                        editPoi = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Button(onClick = { editPoi = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this POI?") },
            confirmButton = {
                Button(onClick = {
                    selectedPoi?.let { viewModel.deletePoi(it.poi) }
                    showDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}