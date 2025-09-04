package com.example.aas_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aas_app.data.dao.*
import com.example.aas_app.data.entity.*

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
        QuestionTaskAssignmentEntity::class
    ],
    version = 12,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun programDao(): ProgramDao
    abstract fun poiDao(): PoiDao
    abstract fun taskDao(): TaskDao
    abstract fun questionDao(): QuestionDao
    abstract fun scaleDao(): ScaleDao
    abstract fun studentDao(): StudentDao
    abstract fun commentDao(): CommentDao
    abstract fun evaluationResultDao(): EvaluationResultDao
    abstract fun instructorStudentAssignmentDao(): InstructorStudentAssignmentDao
    abstract fun instructorProgramAssignmentDao(): InstructorProgramAssignmentDao

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

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create comments table
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

                // Create question_task_assignments table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS question_task_assignments (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        question_id INTEGER NOT NULL,
                        task_id INTEGER NOT NULL,
                        FOREIGN KEY (question_id) REFERENCES pecl_questions(id) ON DELETE RESTRICT,
                        FOREIGN KEY (task_id) REFERENCES pecl_tasks(id) ON DELETE RESTRICT
                    )
                """.trimIndent())

                // Add index on task_id
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS index_question_task_assignments_task_id
                    ON question_task_assignments(task_id)
                """.trimIndent())

                // Add unique index on question_id, task_id
                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS index_question_task_assignments_unique
                    ON question_task_assignments(question_id, task_id)
                """.trimIndent())
            }
        }
    }
}