package com.example.aas_app.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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
        PeclScaleEntity::class
    ],
    version = 12,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun programDao(): ProgramDao

    abstract fun poiDao(): PoiDao

    abstract fun taskDao(): TaskDao

    abstract fun questionDao(): QuestionDao

    abstract fun assignmentDao(): AssignmentDao

    abstract fun instructorStudentAssignmentDao(): InstructorStudentAssignmentDao

    abstract fun evaluationDao(): EvaluationDao

    abstract fun scaleDao(): ScaleDao

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
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create new tables with snake_case columns
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pecl_programs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)"
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pecl_programs_name` ON `pecl_programs` (`name`)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pecl_pois` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `program_id` INTEGER NOT NULL, FOREIGN KEY(`program_id`) REFERENCES `pecl_programs`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pecl_pois_name` ON `pecl_pois` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_pois_program_id` ON `pecl_pois` (`program_id`)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pecl_tasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `poi_id` INTEGER NOT NULL, FOREIGN KEY(`poi_id`) REFERENCES `pecl_pois`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pecl_tasks_name` ON `pecl_tasks` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_tasks_poi_id` ON `pecl_tasks` (`poi_id`)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pecl_questions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sub_task` TEXT NOT NULL, `control_type` TEXT NOT NULL, `scale` TEXT NOT NULL, `critical_task` TEXT NOT NULL)"
                )

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `question_assignments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `question_id` INTEGER NOT NULL, `task_id` INTEGER NOT NULL, FOREIGN KEY(`question_id`) REFERENCES `pecl_questions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`task_id`) REFERENCES `pecl_tasks`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_question_assignments_question_id_task_id` ON `question_assignments` (`question_id`, `task_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_question_assignments_task_id` ON `question_assignments` (`task_id`)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `instructor_student_assignments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `instructor_id` INTEGER NOT NULL, `student_id` INTEGER NOT NULL, `program_id` INTEGER, FOREIGN KEY(`instructor_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`student_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`program_id`) REFERENCES `pecl_programs`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instructor_student_assignments_instructor_id` ON `instructor_student_assignments` (`instructor_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_instructor_student_assignments_student_id` ON `instructor_student_assignments` (`student_id`)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pecl_evaluation_results` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `student_id` INTEGER NOT NULL, `instructor_id` INTEGER NOT NULL, `question_id` INTEGER NOT NULL, `score` REAL NOT NULL, `comment` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, FOREIGN KEY(`student_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`instructor_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`question_id`) REFERENCES `pecl_questions`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_evaluation_results_student_id` ON `pecl_evaluation_results` (`student_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_pecl_evaluation_results_question_id` ON `pecl_evaluation_results` (`question_id`)")

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pecl_scales` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scale_name` TEXT NOT NULL, `options` TEXT NOT NULL)"
                )

                // Add role to users if not exists
                try {
                    db.execSQL("ALTER TABLE users ADD COLUMN `role` TEXT DEFAULT ''")
                } catch (e: Exception) {
                    // Column already exists
                }

                // Migrate data if needed (from old pecl_questions to new structure)
                // For dev, assume prepop handles it
            }
        }
    }
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun getUsersByRole(role: String): List<UserEntity>
}

@Dao
interface ProgramDao {
    @Query("SELECT * FROM pecl_programs")
    suspend fun getAllPrograms(): List<PeclProgramEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgram(program: PeclProgramEntity): Long

    @Update
    suspend fun updateProgram(program: PeclProgramEntity)

    @Delete
    suspend fun deleteProgram(program: PeclProgramEntity)

    @Query("SELECT * FROM pecl_programs WHERE id = :id")
    suspend fun getProgramById(id: Long): PeclProgramEntity?

    @Query("SELECT * FROM pecl_programs WHERE name = :name")
    suspend fun getProgramByName(name: String): PeclProgramEntity?
}

@Dao
interface PoiDao {
    @Query("SELECT * FROM pecl_pois WHERE program_id = :programId")
    suspend fun getPoisForProgram(programId: Long): List<PeclPoiEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: PeclPoiEntity): Long

    @Update
    suspend fun updatePoi(poi: PeclPoiEntity)

    @Delete
    suspend fun deletePoi(poi: PeclPoiEntity)

    @Query("SELECT * FROM pecl_pois WHERE id = :id")
    suspend fun getPoiById(id: Long): PeclPoiEntity?

    @Query("SELECT * FROM pecl_pois WHERE name = :name")
    suspend fun getPoiByName(name: String): PeclPoiEntity?
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM pecl_tasks WHERE poi_id = :poiId")
    suspend fun getTasksForPoi(poiId: Long): List<PeclTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: PeclTaskEntity): Long

    @Update
    suspend fun updateTask(task: PeclTaskEntity)

    @Delete
    suspend fun deleteTask(task: PeclTaskEntity)

    @Query("SELECT * FROM pecl_tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): PeclTaskEntity?

    @Query("SELECT * FROM pecl_tasks WHERE name = :name")
    suspend fun getTaskByName(name: String): PeclTaskEntity?
}

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: PeclQuestionEntity): Long

    @Query("SELECT q.* FROM pecl_questions q INNER JOIN question_assignments a ON q.id = a.question_id WHERE a.task_id = :taskId")
    suspend fun getQuestionsForTask(taskId: Long): List<PeclQuestionEntity>

    @Update
    suspend fun updateQuestion(question: PeclQuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: PeclQuestionEntity)

    @Query("SELECT * FROM pecl_questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): PeclQuestionEntity?
}

@Dao
interface AssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: QuestionAssignmentEntity)
}

@Dao
interface InstructorStudentAssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: InstructorStudentAssignmentEntity): Long

    @Query("SELECT u.* FROM users u INNER JOIN instructor_student_assignments a ON u.id = a.student_id WHERE a.instructor_id = :instructorId")
    suspend fun getStudentsForInstructor(instructorId: Long): List<UserEntity>
}

@Dao
interface EvaluationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: PeclEvaluationResultEntity): Long

    @Query("SELECT * FROM pecl_evaluation_results WHERE student_id = :studentId AND question_id IN (SELECT q.id FROM pecl_questions q INNER JOIN question_assignments a ON q.id = a.question_id INNER JOIN pecl_tasks t ON a.task_id = t.id INNER JOIN pecl_pois p ON t.poi_id = p.id WHERE p.id = :poiId)")
    suspend fun getEvaluationsForStudent(studentId: Long, poiId: Long): List<PeclEvaluationResultEntity>

    @Query("SELECT AVG(score) FROM pecl_evaluation_results WHERE student_id = :studentId AND question_id IN (SELECT q.id FROM pecl_questions q INNER JOIN question_assignments a ON q.id = a.question_id INNER JOIN pecl_tasks t ON a.task_id = t.id INNER JOIN pecl_pois p ON t.poi_id = p.id WHERE p.id = :poiId)")
    suspend fun getAverageScoreForStudent(studentId: Long, poiId: Long): Float?
}

@Dao
interface ScaleDao {
    @Query("SELECT * FROM pecl_scales")
    suspend fun getAllScales(): List<PeclScaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScale(scale: PeclScaleEntity)

    @Update
    suspend fun updateScale(scale: PeclScaleEntity)

    @Delete
    suspend fun deleteScale(scale: PeclScaleEntity)
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "role") val role: String = ""
)

@Entity(tableName = "pecl_programs")
data class PeclProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String
)

@Entity(
    tableName = "pecl_pois",
    foreignKeys = [ForeignKey(
        entity = PeclProgramEntity::class,
        parentColumns = ["id"],
        childColumns = ["program_id"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index(value = ["program_id"])]
)
data class PeclPoiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "program_id") val programId: Long
)

@Entity(
    tableName = "pecl_tasks",
    foreignKeys = [ForeignKey(
        entity = PeclPoiEntity::class,
        parentColumns = ["id"],
        childColumns = ["poi_id"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index(value = ["poi_id"])]
)
data class PeclTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "poi_id") val poiId: Long
)

@Entity(tableName = "pecl_questions")
data class PeclQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "sub_task") val subTask: String,
    @ColumnInfo(name = "control_type") val controlType: String,
    val scale: String,
    @ColumnInfo(name = "critical_task") val criticalTask: String
)

@Entity(
    tableName = "question_assignments",
    foreignKeys = [
        ForeignKey(
            entity = PeclQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PeclTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["question_id"]), Index(value = ["task_id"])]
)
data class QuestionAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "question_id") val questionId: Long,
    @ColumnInfo(name = "task_id") val taskId: Long
)

@Entity(
    tableName = "instructor_student_assignments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["instructor_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PeclProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["program_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["instructor_id"]), Index(value = ["student_id"]), Index(value = ["program_id"])]
)
data class InstructorStudentAssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "instructor_id") val instructorId: Long,
    @ColumnInfo(name = "student_id") val studentId: Long,
    @ColumnInfo(name = "program_id") val programId: Long? = null
)

@Entity(
    tableName = "pecl_evaluation_results",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["instructor_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = PeclQuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["student_id"]), Index(value = ["instructor_id"]), Index(value = ["question_id"])]
)
data class PeclEvaluationResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "student_id") val studentId: Long,
    @ColumnInfo(name = "instructor_id") val instructorId: Long,
    @ColumnInfo(name = "question_id") val questionId: Long,
    val score: Float,
    val comment: String,
    val timestamp: Long
)

@Entity(tableName = "pecl_scales")
data class PeclScaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "scale_name") val scaleName: String,
    val options: String // comma-sep
)