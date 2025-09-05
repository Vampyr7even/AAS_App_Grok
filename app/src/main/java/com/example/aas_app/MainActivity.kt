package com.example.aas_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aas_app.ui.screens.AddQuestionScreen
import com.example.aas_app.ui.screens.BuilderScreen
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
import com.example.aas_app.ui.screens.pecl.PoisTab
import com.example.aas_app.ui.screens.pecl.SubTasksTab
import com.example.aas_app.ui.screens.pecl.TasksTab
import com.example.aas_app.ui.theme.AAS_AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "pecl/{instructorId}") {
        composable(
            route = "pecl/{instructorId}",
            arguments = listOf(navArgument("instructorId") { type = NavType.LongType })
        ) { backStackEntry ->
            PeclScreen(
                navController = navController,
                instructorId = backStackEntry.arguments?.getLong("instructorId") ?: 0L
            )
        }
        composable(
            route = "editQuestion/{questionId}/{taskId}",
            arguments = listOf(
                navArgument("questionId") { type = NavType.LongType },
                navArgument("taskId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            EditQuestionScreen(
                navController = navController,
                questionId = backStackEntry.arguments?.getLong("questionId") ?: 0L,
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            )
        }
        composable("builder") { BuilderScreen(navController = navController) }
        composable(
            route = "editPois/{programId}",
            arguments = listOf(navArgument("programId") { type = NavType.LongType })
        ) { backStackEntry ->
            EditPoisScreen(
                navController = navController,
                programId = backStackEntry.arguments?.getLong("programId") ?: 0L
            )
        }
        composable(
            route = "editTask/{taskId}/{poiId}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.LongType },
                navArgument("poiId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            EditTaskScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L,
                poiId = backStackEntry.arguments?.getLong("poiId") ?: 0L
            )
        }
        composable(
            route = "editTasks/{poiId}",
            arguments = listOf(navArgument("poiId") { type = NavType.LongType })
        ) { backStackEntry ->
            EditTasksScreen(
                navController = navController,
                poiId = backStackEntry.arguments?.getLong("poiId") ?: 0L
            )
        }
        composable(
            route = "editQuestions/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            EditQuestionsScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            )
        }
        composable(
            route = "addQuestion/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            AddQuestionScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            )
        }
        composable(
            route = "editScale/{scaleId}",
            arguments = listOf(navArgument("scaleId") { type = NavType.LongType })
        ) { backStackEntry ->
            EditScaleScreen(
                navController = navController,
                scaleId = backStackEntry.arguments?.getLong("scaleId") ?: 0L
            )
        }
        composable("survey") { SurveyScreen(navController = navController) }
        composable(
            route = "updateUsers/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            UpdateUsersScreen(
                navController = navController,
                role = backStackEntry.arguments?.getString("role")
            )
        }
        composable(
            route = "peclDashboard/{instructorId}",
            arguments = listOf(navArgument("instructorId") { type = NavType.LongType })
        ) { backStackEntry ->
            PeclDashboardScreen(
                navController = navController,
                instructorId = backStackEntry.arguments?.getLong("instructorId") ?: 0L
            )
        }
        composable(
            route = "poisTab/{programId}",
            arguments = listOf(navArgument("programId") { type = NavType.LongType })
        ) { backStackEntry ->
            PoisTab(
                navController = navController,
                programId = backStackEntry.arguments?.getLong("programId") ?: 0L
            )
        }
        composable(
            route = "tasksTab/{poiId}",
            arguments = listOf(navArgument("poiId") { type = NavType.LongType })
        ) { backStackEntry ->
            TasksTab(
                navController = navController,
                poiId = backStackEntry.arguments?.getLong("poiId") ?: 0L
            )
        }
        composable(
            route = "subTasksTab/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            SubTasksTab(
                navController = navController,
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            )
        }
        composable("repository") { RepositoryScreen(navController = navController) }
    }
}