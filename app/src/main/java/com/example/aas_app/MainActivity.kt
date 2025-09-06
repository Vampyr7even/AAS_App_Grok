package com.example.aas_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aas_app.ui.screens.*
import com.example.aas_app.ui.screens.pecl.*
import com.example.aas_app.ui.theme.AAS_AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AAS_AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentRoute by navController.currentBackStackEntryAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                navController = navController,
                currentRoute = currentRoute?.destination?.route,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("demographics") { DemographicsScreen(navController, coroutineScope, snackbarHostState) }
            composable("surveys") { SurveysScreen(navController) }
            composable("pecl") { PeclScreen(navController, coroutineScope, snackbarHostState) }
            composable("examinations") { ExaminationsScreen(navController) }
            composable("analytics") { AnalyticsScreen(navController) }
            composable("administration") { AdministrationScreen(navController) }
            composable("addScale") { AddScaleScreen(navController, coroutineScope, snackbarHostState) }
            composable("editScale/{scaleId}") { backStackEntry ->
                EditScaleScreen(
                    navController,
                    backStackEntry.arguments?.getString("scaleId")?.toLongOrNull() ?: 0L,
                    coroutineScope,
                    snackbarHostState
                )
            }
            composable("editScales") { EditScalesScreen(navController, coroutineScope, snackbarHostState) }
            composable("editPrograms") { EditProgramsScreen(navController, coroutineScope, snackbarHostState) }
            composable("editPois/{programId}") { backStackEntry ->
                EditPoisScreen(
                    navController,
                    backStackEntry.arguments?.getString("programId")?.toLongOrNull() ?: 0L,
                    coroutineScope,
                    snackbarHostState
                )
            }
            composable("editTasks/{poiId}") { backStackEntry ->
                EditTasksScreen(
                    navController,
                    backStackEntry.arguments?.getString("poiId")?.toLongOrNull() ?: 0L,
                    coroutineScope,
                    snackbarHostState
                )
            }
            composable("editQuestions/{taskId}") { backStackEntry ->
                SubTasksTab(
                    navController,
                    backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: 0L,
                    hiltViewModel(),
                    null,
                    snackbarHostState,
                    coroutineScope
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    navController: NavController,
    currentRoute: String?,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            val tabs = listOf(
                "home" to "Home",
                "demographics" to "Demographics",
                "surveys" to "Surveys",
                "pecl" to "PECL",
                "examinations" to "Examinations",
                "analytics" to "Analytics",
                "administration" to "Administration"
            )

            tabs.forEach { (route, title) ->
                val isSelected = currentRoute == route
                TextButton(
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) Color(0xFFE57373) else Color.Transparent,
                            shape = RoundedCornerShape(0.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFFE57373) else Color.Gray,
                            shape = RoundedCornerShape(0.dp)
                        ),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text(title)
                }
            }
        }

        if (currentRoute != "home") {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Apex Analytics Suite",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun DemographicsScreen(navController: NavController, coroutineScope: CoroutineScope, snackbarHostState: SnackbarHostState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Demographics",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun SurveysScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Surveys",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun PeclScreen(navController: NavController, coroutineScope: CoroutineScope, snackbarHostState: SnackbarHostState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PECL",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            onClick = { navController.navigate("editPrograms") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Edit Programs")
        }
    }
}

@Composable
fun ExaminationsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Examinations",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun AnalyticsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun AdministrationScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Administration",
            style = MaterialTheme.typography.headlineMedium
        )
        Button(
            onClick = { navController.navigate("editScales") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text("Edit Scales")
        }
    }
}