package com.example.aas_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.viewmodel.PeclViewModel

@Composable
fun AddQuestionScreen(viewModel: PeclViewModel, taskId: Int) {
    val scales by viewModel.scales.collectAsState(initial = emptyList())
    var subTask by remember { mutableStateOf("") }
    var controlType by remember { mutableStateOf("") }
    var selectedScale by remember { mutableStateOf("") }
    var criticalTask by remember { mutableStateOf("") }
    var expandedScale by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = subTask,
            onValueChange = { newValue -> subTask = newValue },
            label = { Text("Sub Task") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = controlType,
            onValueChange = { newValue -> controlType = newValue },
            label = { Text("Control Type") },
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown for Scale
        TextField(
            value = selectedScale,
            onValueChange = {},
            readOnly = true,
            label = { Text("Scale") },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Dropdown") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )
        DropdownMenu(
            expanded = expandedScale,
            onDismissRequest = { expandedScale = false }
        ) {
            scales.forEach { scale ->
                DropdownMenuItem(
                    text = { Text(scale.scaleName) },
                    onClick = {
                        selectedScale = scale.scaleName
                        expandedScale = false
                    }
                )
            }
        }

        TextField(
            value = criticalTask,
            onValueChange = { newValue -> criticalTask = newValue },
            label = { Text("Critical Task") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val question = PeclQuestionEntity(
                    subTask = subTask,
                    controlType = controlType,
                    scale = selectedScale,
                    criticalTask = criticalTask
                )
                viewModel.insertQuestionWithAssignment(question, taskId)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Add Question")
        }
    }
}