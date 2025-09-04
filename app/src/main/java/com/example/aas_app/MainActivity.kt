package com.example.aas_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aas_app.ui.screens.AddQuestionScreen
import com.example.aas_app.ui.screens.BuilderScreen
import com.example.aas_app.ui.screens.DemographicsScreen
import com.example.aas_app.ui.screens.EditPoisScreen
import com.example.aas_app.ui.screens.EditQuestionScreen
import com.example.aas_app.ui.screens.EditQuestionsScreen
import com.example.aas_app.ui.screens.EditScaleScreen
import com.example.aas_app.ui.screens.EditTaskScreen
import com.example.aas_app.ui.screens.EditTasksScreen
import com.example.aas_app.ui.screens.PeclDashboardScreen
import com.example.aas_app.ui.screens.PeclScreen
import com.example.aas_app.ui.screens.RepositoryScreen
import com.example.aas_app.ui.screens.SurveyScreen
import com.example.aas_app.ui.screens.UpdateUsersScreen
import com.example.aas_app.ui.theme.AAS_AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AAS_AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "demographics") {
        composable("demographics") {
            DemographicsScreen(navController = navController)
        }
        composable("pecl") {
            PeclScreen(navController = navController, instructorId = 0L)
        }
        composable("admin") {
            PlaceholderAdminScreen(navController = navController)
        }
        composable("survey") {
            SurveyScreen(navController = navController)
        }
        composable("builder") {
            BuilderScreen(navController = navController)
        }
        composable("add_question") {
            AddQuestionScreen(navController = navController)
        }
        composable("edit_pois/{programId}") { backStackEntry ->
            EditPoisScreen(
                navController = navController,
                programId = backStackEntry.arguments?.getString("programId")?.toLongOrNull() ?: 0L
            )
        }
        composable("edit_tasks/{poiId}") { backStackEntry ->
            EditTasksScreen(
                navController = navController,
                poiId = backStackEntry.arguments?.getString("poiId")?.toLongOrNull() ?: 0L
            )
        }
        composable("edit_questions/{taskId}") { backStackEntry ->
            EditQuestionsScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: 0L
            )
        }
        composable("edit_task/{taskId}") { backStackEntry ->
            EditTaskScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull() ?: 0L
            )
        }
        composable("edit_question/{questionId}") { backStackEntry ->
            EditQuestionScreen(
                navController = navController,
                questionId = backStackEntry.arguments?.getString("questionId")?.toLongOrNull() ?: 0L
            )
        }
        composable("edit_scale/{scaleId}") { backStackEntry ->
            EditScaleScreen(
                navController = navController,
                scaleId = backStackEntry.arguments?.getString("scaleId")?.toLongOrNull() ?: 0L
            )
        }
        composable("pecl_dashboard/{programId}/{poiId}/{instructorId}") { backStackEntry ->
            PeclDashboardScreen(
                navController = navController,
                programId = backStackEntry.arguments?.getString("programId")?.toLongOrNull() ?: 0L,
                poiId = backStackEntry.arguments?.getString("poiId")?.toLongOrNull() ?: 0L,
                instructorId = backStackEntry.arguments?.getString("instructorId")?.toLongOrNull() ?: 0L
            )
        }
        composable("update_users") {
            UpdateUsersScreen(navController = navController)
        }
        composable("repository") {
            RepositoryScreen(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderAdminScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        androidx.compose.material3.Text(
            text = "Admin Screen (Under Construction)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxSize(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}