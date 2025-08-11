package com.example.aas_app.ui.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.viewmodel.AdminViewModel

@Composable
fun AddScaleScreen(adminViewModel: AdminViewModel) {
    val scaleName = remember { mutableStateOf("") }
    val options = remember { mutableStateOf("") }

    TextField(value = scaleName.value, onValueChange = { scaleName.value = it })
    TextField(value = options.value, onValueChange = { options.value = it })

    Button(onClick = {
        adminViewModel.insertScale(ScaleEntity(scaleName = scaleName.value, options = options.value))
    }) {
        Text("Save")
    }
}