package com.example.aas_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aas_app.ui.NavigationTopBar
import com.example.aas_app.ui.screens.AddQuestionScreen
import com.example.aas_app.ui.screens.AnalyticsScreen
import com.example.aas_app.ui.screens.BuilderScreen
import com.example.aas_app.ui.screens.DemographicsScreen
import com.example.aas_app.ui.screens.EditPoisScreen
import com.example.aas_app.ui.screens.EditProgramsScreen
import com.example.aas_app.ui.screens.EditTasksScreen
import com.example.aas_app.ui.screens.EvaluateScreen
import com.example.aas_app.ui.screens.ExaminationsScreen
import com.example.aas_app.ui.screens.HomeScreen
import com.example.aas_app.ui.screens.PeclDashboardScreen
import com.example.aas_app.ui.screens.PeclScreen
import com.example.aas_app.ui.screens.RepositoryScreen
import com.example.aas_app.ui.screens.SurveyScreen
import com.example.aas_app.ui.screens.UpdateProjectsScreen
import com.example.aas_app.ui.screens.UpdateUsersScreen
import com.example.aas_app.ui.theme.AAS_AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AAS_AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AASAppScaffold()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AASAppScaffold() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        topBar = { if (currentRoute != "home" && !currentRoute.orEmpty().startsWith("pecl")) NavigationTopBar(navController) else null }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "home", modifier = Modifier.padding(innerPadding)) {
            composable("home") { HomeScreen(navController) }
            composable("demographics") { DemographicsScreen(navController) }
            composable("survey") { SurveyScreen(navController) }
            composable("pecl") { PeclScreen(navController) }
            composable("pecl/evaluate") { EvaluateScreen(navController) }
            composable("examinations") { ExaminationsScreen("Examinations") }
            composable("analytics") { AnalyticsScreen("Analytics") }
            composable("admin/programs") { EditProgramsScreen(navController) }
            composable(
                "admin/pois/{programId}",
                arguments = listOf(navArgument("programId") { type = NavType.LongType })
            ) { backStackEntry: NavBackStackEntry ->
                EditPoisScreen(
                    navController,
                    backStackEntry.arguments?.getLong("programId") ?: 0L
                )
            }
            composable(
                "admin/tasks/{poiId}",
                arguments = listOf(navArgument("poiId") { type = NavType.LongType })
            ) { backStackEntry: NavBackStackEntry ->
                EditTasksScreen(
                    navController,
                    backStackEntry.arguments?.getLong("poiId") ?: 0L
                )
            }
            composable("admin/questions") { AddQuestionScreen(navController) }
            composable("pecl/dashboard/{poiId}") { backStackEntry ->
                PeclDashboardScreen(navController, backStackEntry.arguments?.getLong("poiId") ?: 0L)
            }
            composable(
                "pecl/pois/{programId}",
                arguments = listOf(navArgument("programId") { type = NavType.LongType })
            ) { backStackEntry: NavBackStackEntry ->
                EditPoisScreen(
                    navController,
                    backStackEntry.arguments?.getLong("programId") ?: 0L
                )
            }
            composable(
                "pecl/tasks/{poiId}",
                arguments = listOf(navArgument("poiId") { type = NavType.LongType })
            ) { backStackEntry: NavBackStackEntry ->
                EditTasksScreen(
                    navController,
                    backStackEntry.arguments?.getLong("poiId") ?: 0L
                )
            }
            composable("pecl/subtasks") { AddQuestionScreen(navController) }
            composable("pecl/instructors") { UpdateUsersScreen(navController, "instructor") }
            composable("pecl/students") { UpdateUsersScreen(navController, "student") }
            composable("builder") { BuilderScreen(navController) }
            composable("repository") { RepositoryScreen(navController) }
            composable("update_projects") { UpdateProjectsScreen(navController) }
            composable("update_users") { UpdateUsersScreen(navController, null) }
            // Add other routes as needed
        }
    }
}