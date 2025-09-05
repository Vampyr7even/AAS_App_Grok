package com.example.aas_app.ui.screens.pecl

import android.util.Log
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.launch

@Composable
fun PoisTab(navController: NavController, programId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val poisState by viewModel.poisState.observeAsState(AppState.Loading)
    var showDialog by remember { mutableStateOf(false) }
    var selectedPoi by remember { mutableStateOf<PeclPoiEntity?>(null) }
    var newPoiName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(programId) {
        viewModel.loadPoisForProgram(programId)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = poisState) {
            is AppState.Loading -> Text("Loading...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { poi: PeclPoiEntity ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = poi.name, modifier = Modifier.weight(1f))
                            IconButton(onClick = { navController.navigate("editPois/${poi.id}") }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { selectedPoi = poi; showDialog = true }) {
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
                if (newPoiName.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            viewModel.insertPoi(PeclPoiEntity(name = newPoiName), listOf(programId))
                            newPoiName = ""
                            snackbarHostState.showSnackbar("POI added successfully")
                        } catch (e: Exception) {
                            Log.e("PoisTab", "Error adding POI: ${e.message}", e)
                            snackbarHostState.showSnackbar("Error adding POI: ${e.message}")
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("POI name cannot be blank")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add POI")
        }
        SnackbarHost(hostState = snackbarHostState)

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Delete") },
                text = { Text("Delete this POI?") },
                confirmButton = {
                    Button(onClick = {
                        selectedPoi?.let { poi ->
                            coroutineScope.launch {
                                try {
                                    viewModel.deletePoi(poi)
                                    snackbarHostState.showSnackbar("POI deleted successfully")
                                } catch (e: Exception) {
                                    Log.e("PoisTab", "Error deleting POI: ${e.message}", e)
                                    snackbarHostState.showSnackbar("Error deleting POI: ${e.message}")
                                }
                            }
                        }
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
}