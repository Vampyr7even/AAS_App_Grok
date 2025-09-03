package com.example.aas_app.di

import android.app.Application
import android.util.Log
import com.example.aas_app.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        try {
            return AppDatabase.getDatabase(app)
        } catch (e: Exception) {
            Log.e("AppModule", "Error providing database: ${e.message}", e)
            throw e
        }
    }
}