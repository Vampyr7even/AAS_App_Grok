package com.example.aas_app.di

import android.content.Context
import androidx.room.Room
import com.example.aas_app.data.AppDatabase
import com.example.aas_app.data.AppRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "aas_database"
        )
            .addMigrations(AppDatabase.MIGRATION_11_12)
            .fallbackToDestructiveMigration() // For development; remove in production to avoid data loss
            .build()
    }

    @Provides
    @Singleton
    fun provideRepository(db: AppDatabase): AppRepository {
        return AppRepository(db)
    }
}