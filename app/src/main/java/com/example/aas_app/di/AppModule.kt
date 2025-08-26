package com.example.aas_app.di

import android.app.Application
import androidx.room.Room
import com.example.aas_app.data.AppDatabase
import com.example.aas_app.data.AppRepository
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
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "aas_database"
        )
            .addMigrations(
                AppDatabase.MIGRATION_11_12, AppDatabase.MIGRATION_12_13, AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15, AppDatabase.MIGRATION_15_16, AppDatabase.MIGRATION_16_17,
                AppDatabase.MIGRATION_17_18, AppDatabase.MIGRATION_18_19, AppDatabase.MIGRATION_19_20,
                AppDatabase.MIGRATION_20_21
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideAppRepository(db: AppDatabase): AppRepository {
        return AppRepository(db)
    }
}