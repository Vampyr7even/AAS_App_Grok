package com.example.aas_app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import com.example.aas_app.viewmodel.PoiWithPrograms

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeclScreen(navController: NavController) {
    val viewModel: AdminViewModel = hiltViewModel()
    val programsState by viewModel.programsState.observeAsState(AppState.Loading as AppState<List<PeclProgramEntity>>)
    val poisState by viewModel.poisState.observeAsState(AppState.Loading as AppState<List<PoiWithPrograms>>)
    var selectedTab by remember { mutableStateOf<String?>(null) }
    var showAddProgram by remember { mutableStateOf(false) }
    var newProgramName by remember { mutableStateOf("") }
    var selectedProgramToDelete by remember { mutableStateOf<PeclProgramEntity?>(null) }
    var showAddPoiDialog by remember { mutableStateOf(false) }
    var newPoiName by remember { mutableStateOf("") }
    var selectedProgramsForAdd by remember { mutableStateOf(setOf<Long>()) }
    var showEditPoiDialog by remember { mutableStateOf<PoiWithPrograms?>(null) }
    var editPoiName by remember { mutableStateOf("") }
    var selectedProgramsForEdit by remember { mutableStateOf(setOf<Long>()) }
    var selectedPoiToDelete by remember { mutableStateOf<PeclPoiEntity?>(null) }

    LaunchedEffect(selectedTab) {
        if (selectedTab == "Programs") {
            viewModel.loadPrograms()
        } else if (selectedTab == "POI") {
            viewModel.loadPrograms() // For add/edit dialogs
            viewModel.loadAllPoisWithPrograms()
        }
    }

    LaunchedEffect(showEditPoiDialog) {
        showEditPoiDialog?.let { poiWithPrograms ->
            editPoiName = poiWithPrograms.poi.name
            selectedProgramsForEdit = poiWithPrograms.programs.mapNotNull { programName ->
                programsState.data?.find { it.name == programName }?.id
            }.toSet()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Performance Evaluation Checklist",
                fontSize = 24.sp,
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            PeclModuleNavButton("Evaluate", selectedTab == "Evaluate") { navController.navigate("pecl/dashboard/0"); selectedTab = "Evaluate" }
            PeclModuleNavButton("Programs", selectedTab == "Programs") { selectedTab = "Programs" }
            PeclModuleNavButton("POI", selectedTab == "POI") { selectedTab = "POI" }
            PeclModuleNavButton("Tasks", selectedTab == "Tasks") { navController.navigate("pecl/tasks/0"); selectedTab = "Tasks" } // Placeholder poiId
            PeclModuleNavButton("Sub Tasks", selectedTab == "Sub Tasks") { navController.navigate("pecl/subtasks"); selectedTab = "Sub Tasks" }
            PeclModuleNavButton("Instructors", selectedTab == "Instructors") { navController.navigate("pecl/instructors"); selectedTab = "Instructors" }
            PeclModuleNavButton("Students", selectedTab == "Students") { navController.navigate("pecl/students"); selectedTab = "Students" }
        }

        if (selectedTab == "Programs") {
            Text(
                text = "Programs",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            when (val state = programsState) {
                is AppState.Loading -> Text("Loading...")
                is AppState.Success -> {
                    LazyColumn {
                        items(state.data) { program ->
                            var isEditing by remember { mutableStateOf(false) }
                            var editName by remember { mutableStateOf(program.name) }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isEditing) {
                                    TextField(
                                        value = editName,
                                        onValueChange = { editName = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = {
                                        viewModel.updateProgram(program.copy(name = editName))
                                        isEditing = false
                                    }) {
                                        Icon(imageVector = Icons.Filled.Save, contentDescription = "Save")
                                    }
                                    IconButton(onClick = {
                                        editName = program.name
                                        isEditing = false
                                    }) {
                                        Icon(imageVector = Icons.Filled.Cancel, contentDescription = "Cancel")
                                    }
                                } else {
                                    Text(program.name, modifier = Modifier.weight(1f))
                                    IconButton(onClick = {
                                        isEditing = true
                                        editName = program.name
                                    }) {
                                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { selectedProgramToDelete = program }) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                    }
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text("Error: ${state.message}")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAddProgram = !showAddProgram }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Program")
                }
                Text("Add Program", modifier = Modifier.padding(start = 4.dp))
            }

            if (showAddProgram) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = newProgramName,
                        onValueChange = { newProgramName = it },
                        label = { Text("New Program Name") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        viewModel.insertProgram(PeclProgramEntity(0L, newProgramName))
                        newProgramName = ""
                        showAddProgram = false
                    }) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = "Save New Program")
                    }
                }
            }
        } else if (selectedTab == "POI") {
            Text(
                text = "Program of Instruction",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (val state = poisState) {
                is AppState.Loading -> Text("Loading POIs...")
                is AppState.Success -> {
                    LazyColumn {
                        items(state.data) { poiWithPrograms ->
                            Column {
                                Text(poiWithPrograms.poi.name, modifier = Modifier.weight(1f))
                                Text("Programs: ${poiWithPrograms.programs.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { showEditPoiDialog = poiWithPrograms }) {
                                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit")
                                }
                                IconButton(onClick = { selectedPoiToDelete = poiWithPrograms.poi }) {
                                    Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
                is AppState.Error -> Text("Error: ${state.message}")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAddPoiDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add POI")
                }
                Text("Add POI", modifier = Modifier.padding(start = 4.dp))
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
                            Text("Select Programs:")
                            LazyColumn {
                                items(programsState.data.orEmpty()) { program ->
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
                                        Text(program.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (newPoiName.isNotBlank() && selectedProgramsForAdd.isNotEmpty()) {
                                    viewModel.insertPoi(PeclPoiEntity(name = newPoiName), selectedProgramsForAdd.toList())
                                    showAddPoiDialog = false
                                    newPoiName = ""
                                    selectedProgramsForAdd = emptySet()
                                } else {
                                    // Handle error, e.g., toast "Name and programs required"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showAddPoiDialog = false },
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            showEditPoiDialog?.let { poiWithPrograms ->
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
                            Text("Select Programs:")
                            LazyColumn {
                                items(programsState.data.orEmpty()) { program ->
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
                                        Text(program.name)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (editPoiName.isNotBlank() && selectedProgramsForEdit.isNotEmpty()) {
                                    viewModel.updatePoi(poiWithPrograms.poi.copy(name = editPoiName), selectedProgramsForEdit.toList())
                                    showEditPoiDialog = null
                                    editPoiName = ""
                                    selectedProgramsForEdit = emptySet()
                                } else {
                                    // Handle error
                                }
                            },
                            colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showEditPoiDialog = null },
                            colors = ButtonDefaults.buttonColors(Color.Gray),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }

    selectedProgramToDelete?.let { program ->
        AlertDialog(
            onDismissRequest = { selectedProgramToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this program?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteProgram(program)
                        selectedProgramToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedProgramToDelete = null },
                    colors = ButtonDefaults.buttonColors(Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("No")
                }
            }
        )
    }

    selectedPoiToDelete?.let { poi ->
        AlertDialog(
            onDismissRequest = { selectedPoiToDelete = null },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete this POI?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePoi(poi)
                        selectedPoiToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(Color(0xFFE57373)),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { selectedPoiToDelete = null },
                    colors = ButtonDefaults.buttonColors(Color.Gray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun PeclModuleNavButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFE57373) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp, focusedElevation = 0.dp, hoveredElevation = 0.dp, disabledElevation = 0.dp)
    ) {
        Text(text)
    }
}