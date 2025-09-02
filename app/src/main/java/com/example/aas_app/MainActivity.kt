package com.example.aas_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aas_app.ui.screens.*
import com.example.aas_app.ui.theme.AAS_AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AAS_AppTheme {
                AASApp()
            }
        }
    }
}

@Composable
fun AASApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("admin") {
            AdministrationScreen(adminViewModel = hiltViewModel(), navController = navController)
        }
        composable("demographics") {
            DemographicsScreen(navController = navController)
        }
        composable("pecl") {
            PeclScreen(navController = navController)
        }
        composable("pecl/evaluate") {
            EvaluateScreen(navController = navController)
        }
        composable(
            route = "pecl/dashboard/{programId}/{poiId}/{instructorId}",
            arguments = listOf(
                navArgument("programId") { type = NavType.LongType },
                navArgument("poiId") { type = NavType.LongType },
                navArgument("instructorId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            PeclDashboardScreen(
                navController = navController,
                programId = backStackEntry.arguments?.getLong("programId") ?: 0L,
                poiId = backStackEntry.arguments?.getLong("poiId") ?: 0L,
                instructorId = backStackEntry.arguments?.getLong("instructorId") ?: 0L
            )
        }
        composable(
            route = "grading/{programId}/{poiId}/{studentId}/{taskId}",
            arguments = listOf(
                navArgument("programId") { type = NavType.LongType },
                navArgument("poiId") { type = NavType.LongType },
                navArgument("studentId") { type = NavType.LongType },
                navArgument("taskId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            GradingScreen(
                navController = navController,
                programId = backStackEntry.arguments?.getLong("programId") ?: 0L,
                poiId = backStackEntry.arguments?.getLong("poiId") ?: 0L,
                studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L,
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            )
        }
        composable("repository") {
            RepositoryScreen(navController = navController)
        }
        composable("survey") {
            SurveyScreen(navController = navController)
        }
        composable("updateProjects") {
            UpdateProjectsScreen(navController = navController)
        }
        composable("updateUsers") {
            UpdateUsersScreen(navController = navController)
        }
        composable("userSurveys") {
            UserSurveysScreen(navController = navController, moduleName = "User Surveys")
        }
        composable("placeholder") {
            PlaceholderScreen(navController = navController, moduleName = "Placeholder")
        }
    }
}