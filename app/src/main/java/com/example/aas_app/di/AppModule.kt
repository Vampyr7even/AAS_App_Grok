package com.example.aas_app.di

import android.app.Application
import com.example.aas_app.data.AppDatabase
import com.example.aas_app.data.dao.*
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
        return AppDatabase.getDatabase(app)
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    @Singleton
    fun providePeclProgramDao(db: AppDatabase): PeclProgramDao = db.peclProgramDao()

    @Provides
    @Singleton
    fun providePeclPoiDao(db: AppDatabase): PeclPoiDao = db.peclPoiDao()

    @Provides
    @Singleton
    fun providePeclTaskDao(db: AppDatabase): PeclTaskDao = db.peclTaskDao()

    @Provides
    @Singleton
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()

    @Provides
    @Singleton
    fun provideScaleDao(db: AppDatabase): ScaleDao = db.scaleDao()

    @Provides
    @Singleton
    fun providePeclStudentDao(db: AppDatabase): PeclStudentDao = db.peclStudentDao()

    @Provides
    @Singleton
    fun provideCommentDao(db: AppDatabase): CommentDao = db.commentDao()

    @Provides
    @Singleton
    fun provideEvaluationResultDao(db: AppDatabase): EvaluationResultDao = db.evaluationResultDao()

    @Provides
    @Singleton
    fun provideInstructorStudentAssignmentDao(db: AppDatabase): InstructorStudentAssignmentDao = db.instructorStudentAssignmentDao()

    @Provides
    @Singleton
    fun provideInstructorProgramAssignmentDao(db: AppDatabase): InstructorProgramAssignmentDao = db.instructorProgramAssignmentDao()

    @Provides
    @Singleton
    fun providePoiProgramAssignmentDao(db: AppDatabase): PoiProgramAssignmentDao = db.poiProgramAssignmentDao()

    @Provides
    @Singleton
    fun provideTaskPoiAssignmentDao(db: AppDatabase): TaskPoiAssignmentDao = db.taskPoiAssignmentDao()

    @Provides
    @Singleton
    fun provideDemoTemplatesDao(db: AppDatabase): DemoTemplatesDao = db.demoTemplatesDao()

    @Provides
    @Singleton
    fun provideProjectDao(db: AppDatabase): ProjectDao = db.projectDao()

    @Provides
    @Singleton
    fun provideQuestionRepositoryDao(db: AppDatabase): QuestionRepositoryDao = db.questionRepositoryDao()

    @Provides
    @Singleton
    fun provideResponseDao(db: AppDatabase): ResponseDao = db.responseDao()
}