package com.example.aas_app

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.example.aas_app.data.AppDatabase
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.Result
import com.example.aas_app.viewmodel.AdminViewModel
import com.example.aas_app.viewmodel.DemographicsViewModel
import com.example.aas_app.viewmodel.PeclViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel

class ApexAnalyticsApplication : Application(), KoinComponent {
    lateinit var repository: AppRepository
        private set

    override fun onCreate() {
        super.onCreate()
        Log.d("APEXApp", "Initializing Koin and database")

        try {
            startKoin {
                androidContext(this@ApexAnalyticsApplication)
                modules(
                    databaseModule,
                    repositoryModule,
                    viewModelModule
                )
            }
        } catch (e: Exception) {
            Log.e("APEXApp", "Koin initialization failed", e)
            // Optional: Fallback or partial init if possible
        }

        repository = get<AppRepository>()

        CoroutineScope(Dispatchers.IO).launch {
            Log.d("APEXApp", "Starting prepopulation")
            try {
                repository.prePopulateTemplates()
                repository.prePopulateQuestions()
                repository.prePopulateScales()
                repository.prePopulatePeclPrograms()
                repository.prePopulatePeclPois()
                repository.prePopulatePeclTasks()
                repository.prePopulateProjects()
                repository.prePopulatePeclQuestions()

                // Temporary logging for DB verification
                val programsResult = repository.getAllPeclPrograms()
                val programsCount = if (programsResult is Result.Success) programsResult.data.size else "Error"
                Log.d("DBVerify", "Programs count: $programsCount")
                val poisResult = repository.getAllPeclPois()
                val poisCount = if (poisResult is Result.Success) poisResult.data.size else "Error"
                Log.d("DBVerify", "POIs count: $poisCount")
                val tasksResult = repository.getAllPeclTasks()
                val tasksCount = if (tasksResult is Result.Success) tasksResult.data.size else "Error"
                Log.d("DBVerify", "Tasks count: $tasksCount")
                val questionsResult = repository.getAllPeclQuestions()
                val questionsCount = if (questionsResult is Result.Success) questionsResult.data.size else "Error"
                Log.d("DBVerify", "Questions count: $questionsCount")
            } catch (e: Exception) {
                Log.e("APEXApp", "Prepopulation failed", e)
            }
        }
    }

    private val databaseModule = module {
        single {
            Room.databaseBuilder(
                androidContext(),
                AppDatabase::class.java,
                "app_database"
            ).addMigrations(
                AppDatabase.MIGRATION_11_12
            ).fallbackToDestructiveMigration().build()
        }
    }

    private val repositoryModule = module {
        single { AppRepository(get()) }
    }

    private val viewModelModule = module {
        viewModel { PeclViewModel(get()) }
        viewModel { AdminViewModel(get()) }
        viewModel { DemographicsViewModel(get()) }
    }
}