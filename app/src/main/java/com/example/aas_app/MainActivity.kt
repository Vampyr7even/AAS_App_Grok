package com.example.aas_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aas_app.ui.screens.*
import com.example.aas_app.ui.screens.pecl.PoisTab
import com.example.aas_app.ui.screens.pecl.SubTasksTab
import com.example.aas_app.ui.screens.pecl.TasksTab
import com.example.aas_app.ui.theme.AAS_AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope

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
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController = navController) }
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
        composable("builder") { BuilderScreen(navController = navController, coroutineScope = coroutineScope, snackbarHostState = snackbarHostState) }
        composable(
            route = "editPois/{programId}",
            arguments = listOf(navArgument("programId") { type = NavType.LongType })
        ) { backStackEntry ->
            EditPoisScreen(
                navController = navController,
                programId = backStackEntry.arguments?.getLong("programId") ?: 0L,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
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
                poiId = backStackEntry.arguments?.getLong("poiId") ?: 0L,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
            )
        }
        composable(
            route = "editQuestions/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            EditQuestionsScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
            )
        }
        composable(
            route = "addQuestion/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) { backStackEntry ->
            AddQuestionScreen(
                navController = navController,
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
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
            route = "peclDashboard/{programId}/{poiId}/{instructorId}",
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
                taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L,
                adminViewModel = hiltViewModel(),
                errorMessage = null,
                snackbarHostState = snackbarHostState,
                coroutineScope = coroutineScope
            )
        }
        composable("repository") {
            RepositoryScreen(
                navController = navController,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
            )
        }
        composable("demographics") { PlaceholderScreen(navController = navController, "Demographics") }
        composable("examinations") { PlaceholderScreen(navController = navController, "Examinations") }
        composable("analytics") { PlaceholderScreen(navController = navController, "Analytics") }
        composable("admin/programs") { EditProgramsScreen(navController = navController) }
    }
}