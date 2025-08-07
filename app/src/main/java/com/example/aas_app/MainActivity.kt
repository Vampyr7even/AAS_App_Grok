package com.example.aas_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.aas_app.ui.screens.AddScaleScreen
import com.example.aas_app.ui.screens.AdministrationScreen
import com.example.aas_app.ui.screens.AnalyticsScreen
import com.example.aas_app.ui.screens.BuilderScreen
import com.example.aas_app.ui.screens.DemographicsScreen
import com.example.aas_app.ui.screens.EditPoisScreen
import com.example.aas_app.ui.screens.EditProgramScreen
import com.example.aas_app.ui.screens.EditProgramsScreen
import com.example.aas_app.ui.screens.EditQuestionScreen
import com.example.aas_app.ui.screens.EditQuestionsScreen
import com.example.aas_app.ui.screens.EditScaleScreen
import com.example.aas_app.ui.screens.EditScalesScreen
import com.example.aas_app.ui.screens.EditTaskScreen
import com.example.aas_app.ui.screens.EditTasksScreen
import com.example.aas_app.ui.screens.EvaluateScreen
import com.example.aas_app.ui.screens.ExaminationsScreen
import com.example.aas_app.ui.screens.HomeScreen
import com.example.aas_app.ui.screens.PeclScreen
import com.example.aas_app.ui.screens.RepositoryScreen
import com.example.aas_app.ui.screens.SurveyScreen
import com.example.aas_app.ui.screens.UpdateProjectsScreen
import com.example.aas_app.ui.screens.UpdateUsersScreen
import com.example.aas_app.ui.theme.AASAppTheme
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.DemographicsViewModel
import com.example.aas_app.viewmodel.PeclViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AASAppTheme {
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
    val peclViewModel: PeclViewModel = koinViewModel()
    val adminViewModel: AdminViewModel = koinViewModel()
    val demographicsViewModel: DemographicsViewModel = koinViewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("demographics") { DemographicsScreen(navController) }
        composable("surveys") { SurveyScreen(demographicsViewModel) } // Adjusted to use existing SurveyScreen; can replace with placeholder if needed
        composable("pecl") { PeclScreen(navController) }
        composable("examinations") { ExaminationsScreen("Examinations") }
        composable("analytics") { AnalyticsScreen("Analytics") }
        composable("administration") { AdministrationScreen(navController) }
        composable("evaluate/{program}/{poi}", arguments = listOf(
            navArgument("program") { type = NavType.StringType },
            navArgument("poi") { type = NavType.StringType }
        )) { backStackEntry ->
            val program = backStackEntry.arguments?.getString("program") ?: ""
            val poi = backStackEntry.arguments?.getString("poi") ?: ""
            EvaluateScreen(viewModel = peclViewModel, selectedProgram = program, selectedPoi = poi)
        }
        composable("edit_pois") { EditPoisScreen(peclViewModel, navController) }
        composable("add_poi") { /* Implement AddPoiScreen(peclViewModel, navController) */ }
        composable("edit_poi/{poiId}", arguments = listOf(navArgument("poiId") { type = NavType.IntType })) { backStackEntry ->
            val poiId = backStackEntry.arguments?.getInt("poiId") ?: 0
            EditProgramScreen(peclViewModel, poiId)
        }
        composable("edit_programs") { EditProgramsScreen(peclViewModel, navController) }
        composable("add_program") { /* Implement AddProgramScreen(peclViewModel, navController) */ }
        composable("edit_program/{programId}", arguments = listOf(navArgument("programId") { type = NavType.IntType })) { backStackEntry ->
            val programId = backStackEntry.arguments?.getInt("programId") ?: 0
            EditProgramScreen(peclViewModel, programId)
        }
        composable("edit_tasks") { EditTasksScreen(peclViewModel, navController) }
        composable("add_task") { /* Implement AddTaskScreen(peclViewModel, navController) */ }
        composable("edit_task/{taskId}", arguments = listOf(navArgument("taskId") { type = NavType.IntType })) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            EditTaskScreen(peclViewModel, taskId)
        }
        composable("edit_questions") { EditQuestionsScreen(peclViewModel, navController) }
        composable("add_question/{taskId}", arguments = listOf(navArgument("taskId") { type = NavType.IntType })) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            AddQuestionScreen(peclViewModel, taskId)
        }
        composable("edit_question/{questionId}", arguments = listOf(navArgument("questionId") { type = NavType.IntType })) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getInt("questionId") ?: 0
            EditQuestionScreen(peclViewModel, questionId)
        }
        composable("edit_scales") { EditScalesScreen(peclViewModel, navController) }
        composable("add_scale") { AddScaleScreen(navController) }
        composable("edit_scale/{scaleId}", arguments = listOf(navArgument("scaleId") { type = NavType.IntType })) { backStackEntry ->
            val scaleId = backStackEntry.arguments?.getInt("scaleId") ?: 0
            EditScaleScreen(navController, peclViewModel, scaleId)
        }
        composable("builder") { BuilderScreen(demographicsViewModel) }
        composable("survey") { SurveyScreen(demographicsViewModel) }
        composable("repository") { RepositoryScreen(demographicsViewModel) }
        composable("update_projects") { UpdateProjectsScreen(adminViewModel) }
        composable("update_users") { UpdateUsersScreen(adminViewModel) }
    }
}