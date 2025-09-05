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
    version = 20,
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
                    .addMigrations(MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `instructor_student_assignments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `instructor_id` INTEGER NOT NULL,
                        `student_id` INTEGER NOT NULL,
                        `program_id` INTEGER,
                        FOREIGN KEY (`instructor_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
                        FOREIGN KEY (`student_id`) REFERENCES `pecl_students` (`id`) ON DELETE RESTRICT,
                        FOREIGN KEY (`program_id`) REFERENCES `pecl_programs` (`id`) ON DELETE RESTRICT
                    )
                """)
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `instructor_program_assignments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `instructor_id` INTEGER NOT NULL,
                        `program_id` INTEGER NOT NULL,
                        FOREIGN KEY (`instructor_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
                        FOREIGN KEY (`program_id`) REFERENCES `pecl_programs` (`id`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_instructor_program_assignments_instructor_id_program_id`
                    ON `instructor_program_assignments` (`instructor_id`, `program_id`)
                """)
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS `index_instructor_program_assignments_program_id`
                    ON `instructor_program_assignments` (`program_id`)
                """)
            }
        }

        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `poi_program_assignments` (
                        `poi_id` INTEGER NOT NULL,
                        `program_id` INTEGER NOT NULL,
                        PRIMARY KEY (`poi_id`, `program_id`),
                        FOREIGN KEY (`poi_id`) REFERENCES `pecl_pois` (`id`) ON DELETE CASCADE,
                        FOREIGN KEY (`program_id`) REFERENCES `pecl_programs` (`id`) ON DELETE CASCADE
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `task_poi_assignments` (
                        `task_id` INTEGER NOT NULL,
                        `poi_id` INTEGER NOT NULL,
                        PRIMARY KEY (`task_id`, `poi_id`),
                        FOREIGN KEY (`task_id`) REFERENCES `pecl_tasks` (`id`) ON DELETE CASCADE,
                        FOREIGN KEY (`poi_id`) REFERENCES `pecl_pois` (`id`) ON DELETE CASCADE
                    )
                """)
            }
        }

        private val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `demo_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `templateName` TEXT NOT NULL,
                        `selectedItems` TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `projects` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `projectName` TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `question_repository` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `field` TEXT NOT NULL,
                        `inputType` TEXT NOT NULL,
                        `options` TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `demographics_results` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `questionId` INTEGER NOT NULL,
                        `answer` TEXT NOT NULL,
                        `surveyDate` TEXT NOT NULL
                    )
                """)
            }
        }

        private val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No-op migration to force Room to regenerate classes
            }
        }

        private val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No-op migration to force Room to regenerate classes
            }
        }

        private val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No-op migration to force Room to regenerate classes
            }
        }

        private val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No-op migration to force Room to regenerate classes
            }
        }

        private val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Add role to users
                    database.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'instructor'")

                    // Update existing users to 'instructor'
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

                } catch (e: Exception) {
                    Log.e("Migration", "Error in migration 19_20: ${e.message}", e)
                    throw e // Fail migration on error
                }
            }
        }
    }
}