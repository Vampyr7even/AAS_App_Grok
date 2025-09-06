package com.example.aas_app

import android.app.Application
import com.example.aas_app.data.AppRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class AASApplication : Application() {

    @Inject
    lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        runBlocking {
            try {
                repository.prePopulateAll()
            } catch (e: Exception) {
                android.util.Log.e("AASApplication", "Error pre-populating database: ${e.message}", e)
            }
        }
    }
}