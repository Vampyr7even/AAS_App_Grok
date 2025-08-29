package com.example.aas_app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.aas_app.ui.screens.pecl.EvaluateTab
import com.example.aas_app.ui.screens.pecl.InstructorsTab
import com.example.aas_app.ui.screens.pecl.PoisTab
import com.example.aas_app.ui.screens.pecl.ProgramsTab
import com.example.aas_app.ui.screens.pecl.StudentsTab
import com.example.aas_app.ui.screens.pecl.SubTasksTab
import com.example.aas_app.ui.screens.pecl.TasksTab
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.DemographicsViewModel
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun PeclScreen(navController: NavController) {
    val adminViewModel: AdminViewModel = hiltViewModel()
    val demographicsViewModel: DemographicsViewModel = hiltViewModel()
    val peclViewModel: PeclViewModel = hiltViewModel()
    var selectedTab by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
            }
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
            PeclModuleNavButton("Evaluate", selectedTab == "Evaluate") { navController.navigate("pecl/evaluate"); selectedTab = "Evaluate" }
            PeclModuleNavButton("Programs", selectedTab == "Programs") { selectedTab = "Programs" }
            PeclModuleNavButton("POI", selectedTab == "POI") { selectedTab = "POI" }
            PeclModuleNavButton("Tasks", selectedTab == "Tasks") { selectedTab = "Tasks" }
            PeclModuleNavButton("Sub Tasks", selectedTab == "Sub Tasks") { selectedTab = "Sub Tasks" }
            PeclModuleNavButton("Instructors", selectedTab == "Instructors") { selectedTab = "Instructors" }
            PeclModuleNavButton("Students", selectedTab == "Students") { selectedTab = "Students" }
        }

        SnackbarHost(hostState = snackbarHostState)

        when (selectedTab) {
            "Evaluate" -> EvaluateTab(navController, errorMessage, snackbarHostState, coroutineScope)
            "Programs" -> ProgramsTab(adminViewModel, errorMessage, snackbarHostState, coroutineScope)
            "POI" -> PoisTab(adminViewModel, errorMessage, snackbarHostState, coroutineScope)
            "Tasks" -> TasksTab(adminViewModel, errorMessage, snackbarHostState, coroutineScope)
            "Sub Tasks" -> SubTasksTab(adminViewModel, errorMessage, snackbarHostState, coroutineScope)
            "Instructors" -> InstructorsTab(demographicsViewModel, peclViewModel, errorMessage, snackbarHostState, coroutineScope)
            "Students" -> StudentsTab(peclViewModel, errorMessage, snackbarHostState, coroutineScope)
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