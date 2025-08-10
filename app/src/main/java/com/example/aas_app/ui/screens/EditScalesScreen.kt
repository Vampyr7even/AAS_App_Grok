package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
modifier = Modifier.fillMaxSize().padding(16.dp),
verticalArrangement = Arrangement.Center,
horizontalAlignment = Alignment.CenterHorizontally
) {
    when (val state = scalesState) {
        is AppState.Loading -> Text("Loading...")
        is AppState.Success -> {
            LazyColumn {
                items(state.data) { scale ->
                    Row {
                        Text(scale.scaleName)
                        IconButton(onClick = { navController.navigate("edit_scale/${scale.id}") }) { // Assume route added
                            Icon(Icons.Filled.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { selectedScale = scale; showDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
        is AppState.Error -> Text("Error: ${state.message}")
    }

    var newScaleName by remember { mutableStateOf("") }
    TextField(
        value = newScaleName,
        onValueChange = { newScaleName = it },
        label = { Text("New Scale Name") }
    )
    Button(
        onClick = { viewModel.insertScale(ScaleEntity(0, newScaleName, "")) },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text("Add Scale")
    }
}

if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Confirm Delete") },
        text = { Text("Delete this scale?") },
        confirmButton = {
            Button(onClick = {
                selectedScale?.let { viewModel.deleteScale(it) }
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