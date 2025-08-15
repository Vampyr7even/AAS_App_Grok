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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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

@Composable
fun BuilderScreen(navController: NavController) {
    val viewModel: AdminViewModel = hiltViewModel()
    val poisState by viewModel.poisState.observeAsState(AppState.Loading as AppState<List<PoiWithPrograms>>)

    LaunchedEffect(Unit) {
        viewModel.loadAllPoisWithPrograms()
    }

    var showDialog by remember { mutableStateOf(false) }
    var selectedPoi by remember { mutableStateOf<PoiWithPrograms?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (val state = poisState) {
            is AppState.Loading -> Text("Loading...")
            is AppState.Success -> {
                LazyColumn {
                    items(state.data) { poiWithPrograms ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(poiWithPrograms.poi.name)
                            IconButton(onClick = { /* Navigate to edit */ }) {
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

        Button(
            onClick = { viewModel.insertPoi(PeclPoiEntity(name = "New POI"), emptyList()) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Add POI")
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