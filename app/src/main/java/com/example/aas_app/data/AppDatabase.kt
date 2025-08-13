package com.example.aas_app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aas_app.data.dao.DemoTemplatesDao
import com.example.aas_app.data.dao.EvaluationResultDao
import com.example.aas_app.data.dao.InstructorStudentAssignmentDao
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
        PeclProgramEntity::class,
        PeclPoiEntity::class,
        PeclTaskEntity::class,
        PeclQuestionEntity::class,
        QuestionAssignmentEntity::class,
        InstructorStudentAssignmentEntity::class,
        PeclEvaluationResultEntity::class,
        ScaleEntity::class,
        DemoTemplatesEntity::class,
        QuestionRepositoryEntity::class,
        ResponseEntity::class,
        ProjectEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun peclProgramDao(): PeclProgramDao
    abstract fun peclPoiDao(): PeclPoiDao
    abstract fun peclTaskDao(): PeclTaskDao
    abstract fun peclQuestionDao(): PeclQuestionDao
    abstract fun questionAssignmentDao(): QuestionAssignmentDao
    abstract fun instructorStudentAssignmentDao(): InstructorStudentAssignmentDao
    abstract fun evaluationResultDao(): EvaluationResultDao
    abstract fun scaleDao(): ScaleDao
    abstract fun demoTemplatesDao(): DemoTemplatesDao
    abstract fun questionRepositoryDao(): QuestionRepositoryDao
    abstract fun responseDao(): ResponseDao
    abstract fun projectDao(): ProjectDao

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
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add role column to users if not exists
                try {
                    db.execSQL("ALTER TABLE users ADD COLUMN role TEXT DEFAULT ''")
                } catch (e: Exception) {
                    // Column already exists; ignore
                }

                // Create new tables if not exist
                db.execSQL("CREATE TABLE IF NOT EXISTS `pecl_programs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL UNIQUE)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `pecl_pois` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL UNIQUE, `program_id` INTEGER NOT NULL, FOREIGN KEY(`program_id`) REFERENCES `pecl_programs`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_pois_program_id` ON `pecl_pois` (`program_id`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `pecl_tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL UNIQUE, `poi_id` INTEGER NOT NULL, FOREIGN KEY(`poi_id`) REFERENCES `pecl_pois`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_tasks_poi_id` ON `pecl_tasks` (`poi_id`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `question_assignments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `question_id` INTEGER NOT NULL, `task_id` INTEGER NOT NULL, FOREIGN KEY(`question_id`) REFERENCES `pecl_questions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`task_id`) REFERENCES `pecl_tasks`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_question_assignments_question_id_task_id` ON `question_assignments` (`question_id`, `task_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_assignments_task_id` ON `question_assignments` (`task_id`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `instructor_student_assignments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `instructor_id` INTEGER NOT NULL, `student_id` INTEGER NOT NULL, `program_id` INTEGER, FOREIGN KEY(`instructor_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`student_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`program_id`) REFERENCES `pecl_programs`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instructor_student_assignments_instructor_id` ON `instructor_student_assignments` (`instructor_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instructor_student_assignments_student_id` ON `instructor_student_assignments` (`student_id`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `pecl_scales` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scale_name` TEXT NOT NULL, `options` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `demotemplates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `templateName` TEXT NOT NULL, `selectedItems` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `question_repository` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `field` TEXT NOT NULL, `inputType` TEXT NOT NULL, `options` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `demographics_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `questionId` INTEGER NOT NULL, `answer` TEXT NOT NULL, `surveyDate` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `projects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `projectName` TEXT NOT NULL)")

                // Recreate pecl_questions to remove legacy columns (program, poi, task)
                db.execSQL("CREATE TABLE `pecl_questions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sub_task` TEXT NOT NULL, `control_type` TEXT NOT NULL, `scale` TEXT NOT NULL, `critical_task` TEXT NOT NULL)")
                db.execSQL("INSERT INTO `pecl_questions_new` (`id`, `sub_task`, `control_type`, `scale`, `critical_task`) SELECT `id`, `sub_task`, `control_type`, `scale`, `critical_task` FROM `pecl_questions`")
                db.execSQL("DROP TABLE `pecl_questions`")
                db.execSQL("ALTER TABLE `pecl_questions_new` RENAME TO `pecl_questions`")

                // Recreate pecl_evaluation_results with new schema (drop old if exists, as data mapping to question_id is complex; assume dev phase)
                db.execSQL("DROP TABLE IF EXISTS `pecl_evaluation_results`")
                db.execSQL("CREATE TABLE `pecl_evaluation_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `student_id` INTEGER NOT NULL, `instructor_id` INTEGER NOT NULL, `question_id` INTEGER NOT NULL, `score` REAL NOT NULL, `comment` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`student_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`instructor_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`question_id`) REFERENCES `pecl_questions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                db.execSQL("CREATE INDEX `index_pecl_evaluation_results_student_id` ON `pecl_evaluation_results` (`student_id`)")
                db.execSQL("CREATE INDEX `index_pecl_evaluation_results_question_id` ON `pecl_evaluation_results` (`question_id`)")

                // Data migration for hierarchy: Handle comma-separated parsing in app logic (e.g., prepopulate in repository), not here
            }
        }
    }
}