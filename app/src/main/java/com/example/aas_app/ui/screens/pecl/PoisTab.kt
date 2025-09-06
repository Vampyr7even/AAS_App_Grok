package com.example.aas_app.ui.screens.pecl

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.PoiData
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.Result
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoisTab(navController: NavController, programId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val poisState by viewModel.poisState.observeAsState(AppState.Success(emptyList()))
    val programsState by viewModel.programsState.observeAsState(AppState.Success(emptyList()))
    var showAddPoiDialog by remember { mutableStateOf(false) }
    var newPoiName by remember { mutableStateOf("") }
    var selectedProgramsForAdd by remember { mutableStateOf(setOf<Long>()) }
    var showEditPoiDialog by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var editPoiName by remember { mutableStateOf("") }
    var selectedProgramsForEdit by remember { mutableStateOf(setOf<Long>()) }
    var selectedPoiToDelete by remember { mutableStateOf<PeclPoiEntity?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Log.d("PoisTab", "Loading POIs and programs")
        try {
            // Load existing programs and POIs
            viewModel.loadPrograms()
            viewModel.loadPoisForProgram(programId)

            // Insert PoiData into the database
            val localProgramsState = programsState
            val localPoisState = poisState
            val currentPrograms = when (localProgramsState) {
                is AppState.Success -> localProgramsState.data
                else -> emptyList()
            }
            PoiData.poiData.forEach { (poiName, programNames) ->
                coroutineScope.launch {
                    try {
                        // Check if the POI already exists
                        val existingPois = when (localPoisState) {
                            is AppState.Success -> localPoisState.data
                            else -> emptyList()
                        }
                        if (existingPois.none { it.name == poiName }) {
                            // Get or insert program IDs
                            val programIds = mutableListOf<Long>()
                            programNames.forEach { programName ->
                                val existingProgram = currentPrograms.find { it.name == programName }
                                val programId = if (existingProgram != null) {
                                    existingProgram.id
                                } else {
                                    // Insert new program
                                    val newProgram = PeclProgramEntity(name = programName)
                                    when (val result = viewModel.insertProgram(newProgram)) {
                                        is Result.Success -> result.data
                                        is Result.Error -> {
                                            Log.e("PoisTab", "Error inserting program $programName: ${result.exception.message}")
                                            null
                                        }
                                    }
                                }
                                if (programId != null) programIds.add(programId)
                            }

                            // Insert the POI with associated program IDs
                            if (programIds.isNotEmpty()) {
                                viewModel.insertPoi(PeclPoiEntity(name = poiName), programIds)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PoisTab", "Error inserting POI from PoiData: ${e.message}", e)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Error inserting POI: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PoisTab", "Error loading POI data: ${e.message}", e)
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error loading POI data: ${e.message}")
            }
        }
    }

    LaunchedEffect(showEditPoiDialog) {
        showEditPoiDialog?.let { poi ->
            editPoiName = poi.name
            coroutineScope.launch {
                try {
                    val programIds = viewModel.getProgramsForPoi(poi.id).first().map { program -> program.id }.toSet()
                    selectedProgramsForEdit = programIds
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error loading programs for POI: ${e.message}")
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Program of Instruction",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { showAddPoiDialog = true }) {
            Icon(Icons.Filled.Add, contentDescription = "Add POI")
        }
        Text("Add POI", modifier = Modifier.padding(start = 4.dp))
    }

    when (val state = poisState) {
        is AppState.Loading -> Text("Loading POIs...")
        is AppState.Success -> {
            if (state.data.isEmpty()) {
                Text("No POIs have been entered in the database. Add POIs to begin.")
            } else {
                LazyColumn {
                    items(state.data) { poi ->
                        val programsForPoi = remember { mutableStateOf<List<PeclProgramEntity>>(emptyList()) }
                        LaunchedEffect(poi.id) {
                            try {
                                programsForPoi.value = viewModel.getProgramsForPoi(poi.id).first()
                            } catch (e: Exception) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Error loading programs for POI: ${e.message}")
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = poi.name)
                                Text(
                                    text = "Programs: ${programsForPoi.value.joinToString(", ") { it.name }}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            IconButton(onClick = { showEditPoiDialog = poi }) {
                                Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedPoiToDelete = poi }) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
        is AppState.Error -> Text("Error: ${state.message}")
    }

    if (showAddPoiDialog) {
        AlertDialog(
            onDismissRequest = { showAddPoiDialog = false },
            title = { Text("Add POI") },
            text = {
                Column {
                    TextField(
                        value = newPoiName,
                        onValueChange = { newPoiName = it },
                        label = { Text("POI Name") }
                    )
                    Text(text = "Select Programs:")
                    when (val state = programsState) {
                        is AppState.Loading -> Text("Loading programs...")
                        is AppState.Success -> {
                            LazyColumn {
                                items(state.data) { program ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedProgramsForAdd.contains(program.id),
                                            onCheckedChange = { checked ->
                                                selectedProgramsForAdd = if (checked) {
                                                    selectedProgramsForAdd + program.id
                                                } else {
                                                    selectedProgramsForAdd - program.id
                                                }
                                            }
                                        )
                                        Text(text = program.name)
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error loading programs: ${state.message}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPoiName.isNotBlank() && selectedProgramsForAdd.isNotEmpty()) {
                            coroutineScope.launch {
                                try {
                                    viewModel.insertPoi(PeclPoiEntity(name = newPoiName), selectedProgramsForAdd.toList())
                                    showAddPoiDialog = false
                                    newPoiName = ""
                                    selectedProgramsForAdd = emptySet()
                                    Toast.makeText(context, "POI added successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("PoisTab", "Error adding POI: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error adding POI: ${e.message}")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("POI name and at least one program are required")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = newPoiName.isNotBlank() && selectedProgramsForAdd.isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAddPoiDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    showEditPoiDialog?.let { poi ->
        AlertDialog(
            onDismissRequest = { showEditPoiDialog = null },
            title = { Text("Edit POI") },
            text = {
                Column {
                    TextField(
                        value = editPoiName,
                        onValueChange = { editPoiName = it },
                        label = { Text("POI Name") }
                    )
                    Text(text = "Select Programs:")
                    when (val state = programsState) {
                        is AppState.Loading -> Text("Loading programs...")
                        is AppState.Success -> {
                            LazyColumn {
                                items(state.data) { program ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = selectedProgramsForEdit.contains(program.id),
                                            onCheckedChange = { checked ->
                                                selectedProgramsForEdit = if (checked) {
                                                    selectedProgramsForEdit + program.id
                                                } else {
                                                    selectedProgramsForEdit - program.id
                                                }
                                            }
                                        )
                                        Text(text = program.name)
                                    }
                                }
                            }
                        }
                        is AppState.Error -> Text("Error loading programs: ${state.message}")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editPoiName.isNotBlank() && selectedProgramsForEdit.isNotEmpty()) {
                            coroutineScope.launch {
                                try {
                                    viewModel.updatePoi(poi.copy(name = editPoiName), selectedProgramsForEdit.toList())
                                    showEditPoiDialog = null
                                    editPoiName = ""
                                    selectedProgramsForEdit = emptySet()
                                    Toast.makeText(context, "POI updated successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Log.e("PoisTab", "Error updating POI: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error updating POI: ${e.message}")
                                }
                            }
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("POI name and at least one program are required")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp),
                    enabled = editPoiName.isNotBlank() && selectedProgramsForEdit.isNotEmpty()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditPoiDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedPoiToDelete?.let { poi ->
        AlertDialog(
            onDismissRequest = { selectedPoiToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Delete this POI?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                viewModel.deletePoi(poi)
                                selectedPoiToDelete = null
                                Toast.makeText(context, "POI deleted successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("PoisTab", "Error deleting POI: ${e.message}", e)
                                snackbarHostState.showSnackbar("Error deleting POI: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedPoiToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("No")
                }
            }
        )
    }

    SnackbarHost(hostState = snackbarHostState)
}