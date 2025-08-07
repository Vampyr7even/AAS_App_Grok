package com.example.aas_app

import android.app.Application
import android.content.Context
import com.example.aas_app.data.AppDatabase

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Delegate to ApexAnalyticsApplication logic if needed, or customize
    }

    companion object {
        fun getDatabase(context: Context): AppDatabase {
            return AppDatabase.getDatabase(context)
        }
    }
}