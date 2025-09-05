package com.example.aas_app.di

import android.content.Context
import com.example.aas_app.data.AppDatabase
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.dao.*
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideAppRepository(
        database: AppDatabase,
        userDao: UserDao,
        peclProgramDao: PeclProgramDao,
        peclPoiDao: PeclPoiDao,
        peclTaskDao: PeclTaskDao,
        questionDao: QuestionDao,
        scaleDao: ScaleDao,
        peclStudentDao: PeclStudentDao,
        commentDao: CommentDao,
        evaluationResultDao: EvaluationResultDao,
        instructorStudentAssignmentDao: InstructorStudentAssignmentDao,
        instructorProgramAssignmentDao: InstructorProgramAssignmentDao,
        poiProgramAssignmentDao: PoiProgramAssignmentDao,
        taskPoiAssignmentDao: TaskPoiAssignmentDao,
        demoTemplatesDao: DemoTemplatesDao,
        projectDao: ProjectDao,
        questionRepositoryDao: QuestionRepositoryDao,
        responseDao: ResponseDao
    ): AppRepository {
        return AppRepository(
            database,
            userDao,
            peclProgramDao,
            peclPoiDao,
            peclTaskDao,
            questionDao,
            scaleDao,
            peclStudentDao,
            commentDao,
            evaluationResultDao,
            instructorStudentAssignmentDao,
            instructorProgramAssignmentDao,
            poiProgramAssignmentDao,
            taskPoiAssignmentDao,
            demoTemplatesDao,
            projectDao,
            questionRepositoryDao,
            responseDao
        )
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun providePeclProgramDao(database: AppDatabase): PeclProgramDao {
        return database.peclProgramDao()
    }

    @Provides
    @Singleton
    fun providePeclPoiDao(database: AppDatabase): PeclPoiDao {
        return database.peclPoiDao()
    }

    @Provides
    @Singleton
    fun providePeclTaskDao(database: AppDatabase): PeclTaskDao {
        return database.peclTaskDao()
    }

    @Provides
    @Singleton
    fun provideQuestionDao(database: AppDatabase): QuestionDao {
        return database.questionDao()
    }

    @Provides
    @Singleton
    fun provideScaleDao(database: AppDatabase): ScaleDao {
        return database.scaleDao()
    }

    @Provides
    @Singleton
    fun providePeclStudentDao(database: AppDatabase): PeclStudentDao {
        return database.peclStudentDao()
    }

    @Provides
    @Singleton
    fun provideCommentDao(database: AppDatabase): CommentDao {
        return database.commentDao()
    }

    @Provides
    @Singleton
    fun provideEvaluationResultDao(database: AppDatabase): EvaluationResultDao {
        return database.evaluationResultDao()
    }

    @Provides
    @Singleton
    fun provideInstructorStudentAssignmentDao(database: AppDatabase): InstructorStudentAssignmentDao {
        return database.instructorStudentAssignmentDao()
    }

    @Provides
    @Singleton
    fun provideInstructorProgramAssignmentDao(database: AppDatabase): InstructorProgramAssignmentDao {
        return database.instructorProgramAssignmentDao()
    }

    @Provides
    @Singleton
    fun providePoiProgramAssignmentDao(database: AppDatabase): PoiProgramAssignmentDao {
        return database.poiProgramAssignmentDao()
    }

    @Provides
    @Singleton
    fun provideTaskPoiAssignmentDao(database: AppDatabase): TaskPoiAssignmentDao {
        return database.taskPoiAssignmentDao()
    }

    @Provides
    @Singleton
    fun provideDemoTemplatesDao(database: AppDatabase): DemoTemplatesDao {
        return database.demoTemplatesDao()
    }

    @Provides
    @Singleton
    fun provideProjectDao(database: AppDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    @Singleton
    fun provideQuestionRepositoryDao(database: AppDatabase): QuestionRepositoryDao {
        return database.questionRepositoryDao()
    }

    @Provides
    @Singleton
    fun provideResponseDao(database: AppDatabase): ResponseDao {
        return database.responseDao()
    }
}