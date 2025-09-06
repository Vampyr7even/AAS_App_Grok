package com.example.aas_app.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aas_app.data.dao.*
import com.example.aas_app.data.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext

@Database(
    entities = [
        UserEntity::class,
        PeclProgramEntity::class,
        PeclPoiEntity::class,
        PeclTaskEntity::class,
        PeclQuestionEntity::class,
        ScaleEntity::class,
        PeclStudentEntity::class,
        CommentEntity::class,
        PeclEvaluationResultEntity::class,
        PoiProgramAssignmentEntity::class,
        TaskPoiAssignmentEntity::class,
        QuestionAssignmentEntity::class,
        InstructorStudentAssignmentEntity::class,
        InstructorProgramAssignmentEntity::class,
        DemoTemplateEntity::class,
        ProjectEntity::class,
        QuestionRepositoryEntity::class,
        ResponseEntity::class
    ],
    version = 12,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun peclProgramDao(): PeclProgramDao
    abstract fun peclPoiDao(): PeclPoiDao
    abstract fun peclTaskDao(): PeclTaskDao
    abstract fun questionDao(): QuestionDao
    abstract fun scaleDao(): ScaleDao
    abstract fun peclStudentDao(): PeclStudentDao
    abstract fun commentDao(): CommentDao
    abstract fun evaluationResultDao(): EvaluationResultDao
    abstract fun instructorStudentAssignmentDao(): InstructorStudentAssignmentDao
    abstract fun instructorProgramAssignmentDao(): InstructorProgramAssignmentDao
    abstract fun poiProgramAssignmentDao(): PoiProgramAssignmentDao
    abstract fun taskPoiAssignmentDao(): TaskPoiAssignmentDao
    abstract fun demoTemplatesDao(): DemoTemplatesDao
    abstract fun projectDao(): ProjectDao
    abstract fun questionRepositoryDao(): QuestionRepositoryDao
    abstract fun responseDao(): ResponseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aas_database"
                )
                    .addMigrations(MIGRATION_11_12)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d("Migration", "Starting migration from 11 to 12")

                try {
                    // Add role to users
                    database.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'instructor'")
                    database.execSQL("UPDATE users SET role = 'instructor'")

                    // Add program_id to pecl_pois
                    database.execSQL("ALTER TABLE pecl_pois ADD COLUMN program_id INTEGER NOT NULL DEFAULT 0")

                    // Add poi_id to pecl_tasks
                    database.execSQL("ALTER TABLE pecl_tasks ADD COLUMN poi_id INTEGER NOT NULL DEFAULT 0")

                    // Create question_assignments table
                    database.execSQL("""
                        CREATE TABLE IF NOT EXISTS `question_assignments` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `question_id` INTEGER NOT NULL,
                            `task_id` INTEGER NOT NULL,
                            FOREIGN KEY (`question_id`) REFERENCES `pecl_questions` (`id`) ON DELETE RESTRICT,
                            FOREIGN KEY (`task_id`) REFERENCES `pecl_tasks` (`id`) ON DELETE RESTRICT
                        )
                    """)

                    // Enhance pecl_evaluation_results
                    database.execSQL("ALTER TABLE pecl_evaluation_results ADD COLUMN student_id INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE pecl_evaluation_results ADD COLUMN instructor_id INTEGER NOT NULL DEFAULT 0")
                    database.execSQL("ALTER TABLE pecl_evaluation_results ADD COLUMN question_id INTEGER NOT NULL DEFAULT 0")

                    // Drop old comma fields from pecl_questions (assuming migration from strings)
                    // Note: Room doesn't support dropping columns directly; recreate table if needed
                    // For simplicity, assume we keep them or handle in code

                    // Parse and migrate data (example for programs/POIs/tasks/questions)
                    // This is simplified; in production, query old data and insert new
                    // ... (implement parsing logic here if needed for existing data)

                    Log.d("Migration", "Migration from 11 to 12 completed successfully")
                } catch (e: Exception) {
                    Log.e("Migration", "Error in migration 11_12: ${e.message}", e)
                    throw e // Fail migration on error
                }
            }
        }
    }
}