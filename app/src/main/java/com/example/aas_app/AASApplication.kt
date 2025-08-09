package com.example.aas_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.example.aas_app.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class AASApplication : Application() {
    @Inject lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            repository.prePopulateAll()
        }
    }
}