package com.example.aas_app.data

import android.content.Context
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
        InstructorStudentAssignmentEntity::class,
        InstructorProgramAssignmentEntity::class,
        PoiProgramAssignmentEntity::class,
        TaskPoiAssignmentEntity::class,
        QuestionAssignmentEntity::class,
        DemoTemplateEntity::class,
        ProjectEntity::class,
        QuestionRepositoryEntity::class,
        ResponseEntity::class
    ],
    version = 17,
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
                    .addMigrations(MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS comments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        student_id INTEGER NOT NULL,
                        instructor_id INTEGER NOT NULL,
                        comment TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        FOREIGN KEY (student_id) REFERENCES pecl_students(id) ON DELETE RESTRICT,
                        FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE RESTRICT
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS question_assignments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        question_id INTEGER NOT NULL,
                        task_id INTEGER NOT NULL,
                        FOREIGN KEY (question_id) REFERENCES pecl_questions(id) ON DELETE RESTRICT,
                        FOREIGN KEY (task_id) REFERENCES pecl_tasks(id) ON DELETE RESTRICT
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_question_assignments_task_id
                    ON question_assignments(task_id)
                """.trimIndent())

                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_question_assignments_unique
                    ON question_assignments(question_id, task_id)
                """.trimIndent())
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS demo_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        templateName TEXT NOT NULL,
                        selectedItems TEXT NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS projects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        projectName TEXT NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS question_repository (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        field TEXT NOT NULL,
                        inputType TEXT NOT NULL,
                        options TEXT NOT NULL
                    )
                """.trimIndent())

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS responses (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        question_id INTEGER NOT NULL,
                        response_text TEXT NOT NULL,
                        FOREIGN KEY (question_id) REFERENCES pecl_questions(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS responses")

                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS demographics_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        userId INTEGER NOT NULL,
                        questionId INTEGER NOT NULL,
                        answer TEXT NOT NULL,
                        surveyDate TEXT NOT NULL,
                        FOREIGN KEY (questionId) REFERENCES pecl_questions(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No-op, but forces Room to validate schema
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN full_name TEXT")
                database.execSQL("UPDATE users SET full_name = lastName || ', ' || firstName")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // No-op to re-validate schema after fixes
            }
        }
    }
}