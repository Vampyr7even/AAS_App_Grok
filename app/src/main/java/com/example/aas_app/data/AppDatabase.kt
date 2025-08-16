package com.example.aas_app.data

import android.content.Context
import android.util.Log
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
import com.example.aas_app.data.dao.PoiProgramAssignmentDao
import com.example.aas_app.data.dao.ProjectDao
import com.example.aas_app.data.dao.QuestionAssignmentDao
import com.example.aas_app.data.dao.QuestionRepositoryDao
import com.example.aas_app.data.dao.ResponseDao
import com.example.aas_app.data.dao.ScaleDao
import com.example.aas_app.data.dao.TaskPoiAssignmentDao
import com.example.aas_app.data.dao.UserDao
import com.example.aas_app.data.entity.DemoTemplatesEntity
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.PoiProgramAssignmentEntity
import com.example.aas_app.data.entity.ProjectEntity
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import com.example.aas_app.data.entity.QuestionRepositoryEntity
import com.example.aas_app.data.entity.ResponseEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.TaskPoiAssignmentEntity
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
        ProjectEntity::class,
        PoiProgramAssignmentEntity::class,
        TaskPoiAssignmentEntity::class
    ],
    version = 17,
    exportSchema = true
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
    abstract fun poiProgramAssignmentDao(): PoiProgramAssignmentDao
    abstract fun taskPoiAssignmentDao(): TaskPoiAssignmentDao

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
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE users ADD COLUMN role TEXT DEFAULT ''")
                } catch (e: Exception) {
                    // Column already exists; ignore
                }

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

                db.execSQL("CREATE TABLE `pecl_questions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sub_task` TEXT NOT NULL, `control_type` TEXT NOT NULL, `scale` TEXT NOT NULL, `critical_task` TEXT NOT NULL)")
                db.execSQL("INSERT INTO `pecl_questions_new` (`id`, `sub_task`, `control_type`, `scale`, `critical_task`) SELECT `id`, `sub_task`, `control_type`, `scale`, `critical_task` FROM `pecl_questions`")
                db.execSQL("DROP TABLE `pecl_questions`")
                db.execSQL("ALTER TABLE `pecl_questions_new` RENAME TO `pecl_questions`")

                db.execSQL("DROP TABLE IF EXISTS `pecl_evaluation_results`")
                db.execSQL("CREATE TABLE `pecl_evaluation_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `student_id` INTEGER NOT NULL, `instructor_id` INTEGER NOT NULL, `question_id` INTEGER NOT NULL, `score` REAL NOT NULL, `comment` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`student_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`instructor_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`question_id`) REFERENCES `pecl_questions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                db.execSQL("CREATE INDEX `index_pecl_evaluation_results_student_id` ON `pecl_evaluation_results` (`student_id`)")
                db.execSQL("CREATE INDEX `index_pecl_evaluation_results_question_id` ON `pecl_evaluation_results` (`question_id`)")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_pois_program_id` ON `pecl_pois` (`program_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_tasks_poi_id` ON `pecl_tasks` (`poi_id`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_question_assignments_question_id_task_id` ON `question_assignments` (`question_id`, `task_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_assignments_task_id` ON `question_assignments` (`task_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instructor_student_assignments_instructor_id` ON `instructor_student_assignments` (`instructor_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instructor_student_assignments_student_id` ON `instructor_student_assignments` (`student_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instructor_student_assignments_program_id` ON `instructor_student_assignments` (`program_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_evaluation_results_student_id` ON `pecl_evaluation_results` (`student_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_evaluation_results_instructor_id` ON `pecl_evaluation_results` (`instructor_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_evaluation_results_question_id` ON `pecl_evaluation_results` (`question_id`)")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Empty migration to force schema validation and refresh
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `poi_program_assignments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `poi_id` INTEGER NOT NULL, `program_id` INTEGER NOT NULL, FOREIGN KEY(`poi_id`) REFERENCES `pecl_pois`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`program_id`) REFERENCES `pecl_programs`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_poi_program_assignments_poi_id_program_id` ON `poi_program_assignments` (`poi_id`, `program_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_poi_program_assignments_program_id` ON `poi_program_assignments` (`program_id`)")

                db.execSQL("INSERT OR IGNORE INTO `poi_program_assignments` (`poi_id`, `program_id`) SELECT `id`, `program_id` FROM `pecl_pois` WHERE `program_id` IS NOT NULL")

                db.execSQL("CREATE TABLE `pecl_pois_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL UNIQUE)")
                db.execSQL("INSERT INTO `pecl_pois_new` (`id`, `name`) SELECT `id`, `name` FROM `pecl_pois`")
                db.execSQL("DROP TABLE `pecl_pois`")
                db.execSQL("ALTER TABLE `pecl_pois_new` RENAME TO `pecl_pois`")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `task_poi_assignments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `task_id` INTEGER NOT NULL, `poi_id` INTEGER NOT NULL, FOREIGN KEY(`task_id`) REFERENCES `pecl_tasks`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`poi_id`) REFERENCES `pecl_pois`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_task_poi_assignments_task_id_poi_id` ON `task_poi_assignments` (`task_id`, `poi_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_task_poi_assignments_poi_id` ON `task_poi_assignments` (`poi_id`)")

                db.execSQL("INSERT OR IGNORE INTO `task_poi_assignments` (`task_id`, `poi_id`) SELECT `id`, `poi_id` FROM `pecl_tasks` WHERE `poi_id` IS NOT NULL")

                db.execSQL("CREATE TABLE `pecl_tasks_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL UNIQUE)")
                db.execSQL("INSERT INTO `pecl_tasks_new` (`id`, `name`) SELECT `id`, `name` FROM `pecl_tasks`")
                db.execSQL("DROP TABLE `pecl_tasks`")
                db.execSQL("ALTER TABLE `pecl_tasks_new` RENAME TO `pecl_tasks`")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE pecl_questions ADD COLUMN task_id INTEGER NOT NULL DEFAULT 0")
                } catch (e: Exception) {
                    Log.e("Migration16_17", "task_id column already exists", e)
                }
                db.execSQL("CREATE TABLE pecl_questions_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, task_id INTEGER NOT NULL DEFAULT 0, sub_task TEXT NOT NULL, control_type TEXT NOT NULL, scale TEXT NOT NULL, critical_task TEXT NOT NULL, FOREIGN KEY(task_id) REFERENCES pecl_tasks(id) ON UPDATE NO ACTION ON DELETE RESTRICT )")
                db.execSQL("INSERT INTO pecl_questions_new (id, task_id, sub_task, control_type, scale, critical_task) SELECT id, 0, sub_task, control_type, scale, critical_task FROM pecl_questions")
                db.execSQL("DROP TABLE pecl_questions")
                db.execSQL("ALTER TABLE pecl_questions_new RENAME TO pecl_questions")
                db.execSQL("CREATE INDEX index_pecl_questions_task_id ON pecl_questions (task_id)")
            }
        }
    }
}