package com.example.aas_app.ui.screens

import android.util.Log
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.ui.screens.pecl.*
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.DemographicsViewModel
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.launch

@Composable
fun PeclScreen(navController: NavController, instructorId: Long) {
    val adminViewModel: AdminViewModel = hiltViewModel()
    val demographicsViewModel: DemographicsViewModel = hiltViewModel()
    val peclViewModel: PeclViewModel = hiltViewModel()
    var selectedTab by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Log.d("PeclScreen", "Entered PeclScreen with instructorId=$instructorId")
        try {
            demographicsViewModel.loadInstructors()
            adminViewModel.loadPrograms()
        } catch (e: Exception) {
            Log.e("PeclScreen", "Error initializing ViewModels: ${e.message}", e)
            errorMessage = "Error initializing ViewModels: ${e.message}"
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Log.w("PeclScreen", "Showing error: $it")
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
            errorMessage = null
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
            PeclModuleNavButton("Evaluate", selectedTab == "Evaluate") {
                Log.d("PeclScreen", "Selected Evaluate tab")
                selectedTab = "Evaluate"
            }
            PeclModuleNavButton("Programs", selectedTab == "Programs") {
                Log.d("PeclScreen", "Selected Programs tab")
                selectedTab = "Programs"
            }
            PeclModuleNavButton("POI", selectedTab == "POI") {
                Log.d("PeclScreen", "Selected POI tab")
                selectedTab = "POI"
            }
            PeclModuleNavButton("Tasks", selectedTab == "Tasks") {
                Log.d("PeclScreen", "Selected Tasks tab")
                selectedTab = "Tasks"
            }
            PeclModuleNavButton("Sub Tasks", selectedTab == "Sub Tasks") {
                Log.d("PeclScreen", "Selected Sub Tasks tab")
                selectedTab = "Sub Tasks"
            }
            PeclModuleNavButton("Instructors", selectedTab == "Instructors") {
                Log.d("PeclScreen", "Selected Instructors tab")
                selectedTab = "Instructors"
            }
            PeclModuleNavButton("Students", selectedTab == "Students") {
                Log.d("PeclScreen", "Selected Students tab")
                selectedTab = "Students"
            }
        }

        SnackbarHost(hostState = snackbarHostState)

        when (selectedTab) {
            "Evaluate" -> {
                Log.d("PeclScreen", "Rendering EvaluateTab")
                EvaluateTab(navController, errorMessage, snackbarHostState, coroutineScope)
            }
            "Programs" -> {
                Log.d("PeclScreen", "Rendering ProgramsTab")
                ProgramsTab(adminViewModel, errorMessage, snackbarHostState, coroutineScope)
            }
            "POI" -> {
                Log.d("PeclScreen", "Rendering PoisTab")
                PoisTab(navController, 0L)
            }
            "Tasks" -> {
                Log.d("PeclScreen", "Rendering TasksTab")
                TasksTab(navController, 0L)
            }
            "Sub Tasks" -> {
                Log.d("PeclScreen", "Rendering SubTasksTab")
                SubTasksTab(navController, 0L, adminViewModel, errorMessage, snackbarHostState, coroutineScope)
            }
            "Instructors" -> {
                Log.d("PeclScreen", "Rendering InstructorsTab")
                InstructorsTab(navController, demographicsViewModel, peclViewModel, errorMessage, snackbarHostState, coroutineScope)
            }
            "Students" -> {
                Log.d("PeclScreen", "Rendering StudentsTab")
                StudentsTab(navController, instructorId, 0L)
            }
            null -> {}
        }
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
        )
    ) {
        Text(text)
    }
}