package com.example.aas_app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.example.aas_app.data.AppRepository
import javax.inject.Inject

@HiltAndroidApp
class AASApplication : Application() {
    @Inject
    lateinit var repository: AppRepository

    override fun onCreate() {
        super.onCreate()
        repository.prePopulateAll()
    }
}