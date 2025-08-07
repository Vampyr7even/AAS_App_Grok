package com.example.aas_app

import android.app.Application
import android.util.Log
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.AppResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApexAnalyticsApplication : Application() {
    lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            repository.prePopulateAll()

            // Verify prepopulation
            val programsResult = repository.getAllPrograms()
            when (programsResult) {
                is AppResult.Success -> Log.d("PrepopVerify", "Programs: ${programsResult.data.size}")
                is AppResult.Error -> Log.e("PrepopVerify", "Error fetching programs: ${programsResult.message}")
            }

            val firstProgramId = if (programsResult is AppResult.Success && programsResult.data.isNotEmpty()) programsResult.data.first().id else 0L

            val poisResult = repository.getPoisForProgram(firstProgramId)
            when (poisResult) {
                is AppResult.Success -> Log.d("PrepopVerify", "POIs: ${poisResult.data.size}")
                is AppResult.Error -> Log.e("PrepopVerify", "Error fetching POIs: ${poisResult.message}")
            }

            val firstPoiId = if (poisResult is AppResult.Success && poisResult.data.isNotEmpty()) poisResult.data.first().id else 0L

            val tasksResult = repository.getTasksForPoi(firstPoiId)
            when (tasksResult) {
                is AppResult.Success -> Log.d("PrepopVerify", "Tasks: ${tasksResult.data.size}")
                is AppResult.Error -> Log.e("PrepopVerify", "Error fetching tasks: ${tasksResult.message}")
            }

            val firstTaskId = if (tasksResult is AppResult.Success && tasksResult.data.isNotEmpty()) tasksResult.data.first().id else 0L

            val questionsResult = repository.getQuestionsForTask(firstTaskId)
            when (questionsResult) {
                is AppResult.Success -> Log.d("PrepopVerify", "Questions: ${questionsResult.data.size}")
                is AppResult.Error -> Log.e("PrepopVerify", "Error fetching questions: ${questionsResult.message}")
            }
        }
    }
}