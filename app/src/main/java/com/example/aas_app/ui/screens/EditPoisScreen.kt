package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.AppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPoisScreen(navController: NavController, programId: Long) {
    val viewModel = hiltViewModel<AdminViewModel>()
    val poisState by viewModel.poisState.observeAsState(AppState.Loading)
    var newPoiName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadPois()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit POIs",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when (val state = poisState) {
            is AppState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is AppState.Success<*> -> {
                val pois = (state as AppState.Success<List<PeclPoiEntity>>).data
                LazyColumn {
                    items(pois) { poi: PeclPoiEntity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = poi.name)
                            Row {
                                IconButton(onClick = { navController.navigate("editPoi/${poi.id}") }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit POI")
                                }
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            viewModel.deletePoi(poi)
                                            snackbarHostState.showSnackbar("POI deleted successfully")
                                        } catch (e: Exception) {
                                            Log.e("EditPoisScreen", "Error deleting POI: ${e.message}", e)
                                            snackbarHostState.showSnackbar("Error deleting POI: ${e.message}")
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete POI")
                                }
                            }
                        }
                    }
                }
            }
            is AppState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = newPoiName,
            onValueChange = { newPoiName = it },
            label = { Text("New POI Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (newPoiName.isNotBlank()) {
                    coroutineScope.launch {
                        try {
                            viewModel.insertPoi(PeclPoiEntity(name = newPoiName), emptyList())
                            newPoiName = ""
                            snackbarHostState.showSnackbar("POI added successfully")
                        } catch (e: Exception) {
                            Log.e("EditPoisScreen", "Error adding POI: ${e.message}", e)
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
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add POI")
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}