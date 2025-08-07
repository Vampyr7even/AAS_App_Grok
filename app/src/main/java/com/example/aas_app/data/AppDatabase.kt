package com.example.aas_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aas_app.data.dao.DemoTemplatesDao
import com.example.aas_app.data.dao.InstructorStudentAssignmentDao
import com.example.aas_app.data.dao.PeclEvaluationResultDao
import com.example.aas_app.data.dao.PeclPoiDao
import com.example.aas_app.data.dao.PeclProgramDao
import com.example.aas_app.data.dao.PeclQuestionDao
import com.example.aas_app.data.dao.PeclTaskDao
import com.example.aas_app.data.dao.ProjectDao
import com.example.aas_app.data.dao.QuestionAssignmentDao
import com.example.aas_app.data.dao.QuestionRepositoryDao
import com.example.aas_app.data.dao.ResponseDao
import com.example.aas_app.data.dao.ScaleDao
import com.example.aas_app.data.dao.UserDao
import com.example.aas_app.data.entity.DemoTemplatesEntity
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.ProjectEntity
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import com.example.aas_app.data.entity.QuestionRepositoryEntity
import com.example.aas_app.data.entity.ResponseEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        QuestionRepositoryEntity::class,
        DemoTemplatesEntity::class,
        ResponseEntity::class,
        ProjectEntity::class,
        PeclQuestionEntity::class,
        PeclProgramEntity::class,
        PeclPoiEntity::class,
        ScaleEntity::class,
        PeclTaskEntity::class,
        PeclEvaluationResultEntity::class,
        QuestionAssignmentEntity::class,
        InstructorStudentAssignmentEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questionRepositoryDao(): QuestionRepositoryDao
    abstract fun demoTemplatesDao(): DemoTemplatesDao
    abstract fun responseDao(): ResponseDao
    abstract fun projectDao(): ProjectDao
    abstract fun peclQuestionDao(): PeclQuestionDao
    abstract fun peclProgramDao(): PeclProgramDao
    abstract fun peclPoiDao(): PeclPoiDao
    abstract fun scaleDao(): ScaleDao
    abstract fun peclTaskDao(): PeclTaskDao
    abstract fun peclEvaluationResultDao(): PeclEvaluationResultDao
    abstract fun questionAssignmentDao(): QuestionAssignmentDao
    abstract fun instructorStudentAssignmentDao(): InstructorStudentAssignmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addMigrations(
                    MIGRATION_11_12
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Step 1: Add 'role' to users (default null for existing)
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT")

                // Step 2: Add FK fields to existing tables
                // For pecl_poi: Add program_id FK
                db.execSQL("ALTER TABLE pecl_poi ADD COLUMN program_id INTEGER NOT NULL DEFAULT 0")

                // For pecl_tasks: Add poi_id FK
                db.execSQL("ALTER TABLE pecl_tasks ADD COLUMN poi_id INTEGER NOT NULL DEFAULT 0")

                // Step 3: Migrate data from old pecl_questions (parse commas, insert into new hierarchy)
                // First, extract and insert unique programs from pecl_questions.program (comma-sep)
                val cursorPrograms = db.query("SELECT DISTINCT program FROM pecl_questions")
                while (cursorPrograms.moveToNext()) {
                    val programsStr = cursorPrograms.getString(0) ?: continue
                    val uniquePrograms = programsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                    for (programName in uniquePrograms) {
                        db.execSQL("INSERT OR IGNORE INTO pecl_programs (peclProgram) VALUES (?)", arrayOf(programName))
                    }
                }
                cursorPrograms.close()

                // Similarly for POIs (from pecl_questions.poi), linking to programs
                val cursorPois = db.query("SELECT program, poi FROM pecl_questions")
                while (cursorPois.moveToNext()) {
                    val programsStr = cursorPois.getString(0) ?: continue
                    val poisStr = cursorPois.getString(1) ?: continue
                    val programNames = programsStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
                    val poiNames = poisStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()

                    for (poiName in poiNames) {
                        for (programName in programNames) {  // Link to all programs for shared POIs
                            val programCursor = db.query("SELECT id FROM pecl_programs WHERE peclProgram = ?", arrayOf(programName))
                            if (programCursor.moveToFirst()) {
                                val programId = programCursor.getInt(0)
                                db.execSQL("INSERT OR IGNORE INTO pecl_poi (peclPoi, program_id) VALUES (?, ?)", arrayOf(poiName, programId))
                            }
                            programCursor.close()
                        }
                    }
                }
                cursorPois.close()

                // For tasks: Extract unique from pecl_questions.task, link to POIs
                val cursorTasks = db.query("SELECT poi, task FROM pecl_questions")
                while (cursorTasks.moveToNext()) {
                    val poisStr = cursorTasks.getString(0) ?: continue
                    val taskName = cursorTasks.getString(1) ?: continue
                    val poiNames = poisStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()

                    for (poiName in poiNames) {
                        val poiCursor = db.query("SELECT id FROM pecl_poi WHERE peclPoi = ?", arrayOf(poiName))
                        if (poiCursor.moveToFirst()) {
                            val poiId = poiCursor.getInt(0)
                            db.execSQL("INSERT OR IGNORE INTO pecl_tasks (peclTask, poi_id) VALUES (?, ?)", arrayOf(taskName, poiId))
                        }
                        poiCursor.close()
                    }
                }
                cursorTasks.close()

                // Populate question_assignments: For each old question, find task_id(s) from parsed task
                val cursorAssignments = db.query("SELECT id, task FROM pecl_questions")
                while (cursorAssignments.moveToNext()) {
                    val questionId = cursorAssignments.getInt(0)
                    val taskName = cursorAssignments.getString(1) ?: continue

                    val taskCursor = db.query("SELECT id FROM pecl_tasks WHERE peclTask = ?", arrayOf(taskName))
                    while (taskCursor.moveToNext()) {
                        val taskId = taskCursor.getInt(0)
                        db.execSQL("INSERT INTO question_assignments (question_id, task_id) VALUES (?, ?)", arrayOf(questionId, taskId))
                    }
                    taskCursor.close()
                }
                cursorAssignments.close()

                // Step 4: Create new junction tables with FKs and indices
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `question_assignments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `question_id` INTEGER NOT NULL,
                        `task_id` INTEGER NOT NULL,
                        FOREIGN KEY(`question_id`) REFERENCES `pecl_questions`(`id`) ON DELETE RESTRICT,
                        FOREIGN KEY(`task_id`) REFERENCES `pecl_tasks`(`id`) ON DELETE RESTRICT
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_question_assignments_question_id_task_id` ON `question_assignments` (`question_id`, `task_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_assignments_task_id` ON `question_assignments` (`task_id`)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `instructor_student_assignments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `instructor_id` INTEGER NOT NULL,
                        `student_id` INTEGER NOT NULL,
                        `program_id` INTEGER,
                        FOREIGN KEY(`instructor_id`) REFERENCES `users`(`id`) ON DELETE RESTRICT,
                        FOREIGN KEY(`student_id`) REFERENCES `users`(`id`) ON DELETE RESTRICT,
                        FOREIGN KEY(`program_id`) REFERENCES `pecl_programs`(`id`) ON DELETE SET NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_instructor_student_assignments_instructor_id_student_id` ON `instructor_student_assignments` (`instructor_id`, `student_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instructor_student_assignments_program_id` ON `instructor_student_assignments` (`program_id`)")

                // Step 5: Migrate questions: Insert core fields into pecl_questions, drop old program/poi/task
                db.execSQL("""
                    CREATE TABLE new_pecl_questions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        subTask TEXT NOT NULL,
                        controlType TEXT NOT NULL,
                        scale TEXT NOT NULL,
                        criticalTask TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO new_pecl_questions (id, subTask, controlType, scale, criticalTask)
                    SELECT id, subTask, controlType, scale, criticalTask FROM pecl_questions
                """.trimIndent())
                db.execSQL("DROP TABLE pecl_questions")
                db.execSQL("ALTER TABLE new_pecl_questions RENAME TO pecl_questions")

                // Step 6: Add FKs to pecl_evaluation_results (for student, instructor, question)
                db.execSQL("ALTER TABLE pecl_evaluation_results ADD COLUMN student_id INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pecl_evaluation_results ADD COLUMN instructor_id INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE pecl_evaluation_results ADD COLUMN question_id INTEGER NOT NULL DEFAULT 0")

                // Step 7: Migrate evaluation results data to new table with FKs
                db.execSQL("""
                    CREATE TABLE new_pecl_evaluation_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        evaluatorId INTEGER NOT NULL,
                        evaluateeName TEXT,
                        program TEXT NOT NULL,
                        poi TEXT NOT NULL,
                        task TEXT NOT NULL,
                        subTask TEXT NOT NULL,
                        score TEXT NOT NULL,
                        comment TEXT,
                        timestamp TEXT NOT NULL,
                        student_id INTEGER NOT NULL,
                        instructor_id INTEGER NOT NULL,
                        question_id INTEGER NOT NULL,
                        FOREIGN KEY(student_id) REFERENCES users(id) ON DELETE RESTRICT,
                        FOREIGN KEY(instructor_id) REFERENCES users(id) ON DELETE RESTRICT,
                        FOREIGN KEY(question_id) REFERENCES pecl_questions(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
                // Migrate existing data (program/poi/task/subTask to new, set defaults for new fields)
                db.execSQL("""
                    INSERT INTO new_pecl_evaluation_results (id, evaluatorId, evaluateeName, program, poi, task, subTask, score, comment, timestamp, student_id, instructor_id, question_id)
                    SELECT id, evaluatorId, evaluateeName, program, poi, task, subTask, score, comment, timestamp, 0, 0, 0 FROM pecl_evaluation_results
                """.trimIndent())
                db.execSQL("DROP TABLE pecl_evaluation_results")
                db.execSQL("ALTER TABLE new_pecl_evaluation_results RENAME TO pecl_evaluation_results")

                // Additional: Add UNIQUE on names for programs, POIs, tasks to prevent dupes
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pecl_programs_peclProgram ON pecl_programs (peclProgram)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pecl_poi_peclPoi ON pecl_poi (peclPoi)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_pecl_tasks_peclTask ON pecl_tasks (peclTask)")
            }
        }
    }
}