package com.example.aas_app.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.State
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(
    navController: NavController,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    val viewModel: AdminViewModel = hiltViewModel()
    val poisState by viewModel.poisState.observeAsState(State.Success(emptyList()))
    val programsState by viewModel.programsState.observeAsState(State.Success(emptyList()))
    var showAddPoiDialog by remember { mutableStateOf(false) }
    var newPoiName by remember { mutableStateOf("") }
    var selectedProgramsForAdd by remember { mutableStateOf(setOf<Long>()) }
    var showDeleteDialog by remember { mutableStateOf<PeclPoiEntity?>(null) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadPrograms()
        viewModel.loadPoisForProgram(0L) // Load all POIs initially
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "POI Builder",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
            is State.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            is State.Success -> {
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
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = poi.name)
                                    Text(
                                        text = "Programs: ${programsForPoi.value.joinToString(", ") { it.name }}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                IconButton(onClick = { navController.navigate("editPois/${poi.id}") }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { showDeleteDialog = poi }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
            is State.Error -> Text(
                text = "Error: ${state.message}",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
                            label = { Text("POI Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(text = "Select Programs:", modifier = Modifier.padding(top = 8.dp))
                        when (val state = programsState) {
                            is State.Loading -> Text("Loading programs...")
                            is State.Success -> {
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
                            is State.Error -> Text("Error loading programs: ${state.message}")
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
                                        Log.e("BuilderScreen", "Error adding POI: ${e.message}", e)
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

        if (showDeleteDialog != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("Confirm Delete") },
                text = { Text("Delete this POI?") },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    showDeleteDialog?.let { viewModel.deletePoi(it) }
                                    snackbarHostState.showSnackbar("POI deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("BuilderScreen", "Error deleting POI: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting POI: ${e.message}")
                                }
                            }
                            showDeleteDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = null },
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
}