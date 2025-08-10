package com.example.aas_app

import android.app.Application
import android.util.Log
import com.example.aas_app.data.AppDatabase
import com.example.aas_app.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ApexAnalyticsApplication : Application() {
    lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(AppDatabase.getDatabase(this))

        CoroutineScope(Dispatchers.IO).launch {
            repository.prePopulateAll()

            // Verify prepopulation
            try {
                val programs = repository.getAllPrograms().first()
                Log.d("PrepopVerify", "Programs: ${programs.size}")

                val firstProgramId = if (programs.isNotEmpty()) programs.first().id else 0L

                val pois = repository.getPoisForProgram(firstProgramId).first()
                Log.d("PrepopVerify", "POIs: ${pois.size}")

                val firstPoiId = if (pois.isNotEmpty()) pois.first().id else 0L

                val tasks = repository.getTasksForPoi(firstPoiId).first()
                Log.d("PrepopVerify", "Tasks: ${tasks.size}")

                val firstTaskId = if (tasks.isNotEmpty()) tasks.first().id else 0L

                val questions = repository.getQuestionsForTask(firstTaskId).first()
                Log.d("PrepopVerify", "Questions: ${questions.size}")
            } catch (e: Exception) {
                Log.e("PrepopVerify", "Error during verification: ${e.message}", e)
            }
        }
    }
}