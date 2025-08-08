package com.example.aas_app.data

import android.util.Log
import androidx.room.withTransaction
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclQuestionAssignmentEntity
import com.example.aas_app.data.entity.PeclScaleEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : AppResult<Nothing>()
}

class AppRepository @Inject constructor(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val programDao = db.programDao()
    private val poiDao = db.poiDao()
    private val taskDao = db.taskDao()
    private val questionDao = db.questionDao()
    private val questionAssignmentDao = db.questionAssignmentDao()
    private val scaleDao = db.scaleDao()
    private val evaluationResultDao = db.evaluationResultDao()
    private val instructorStudentAssignmentDao = db.instructorStudentAssignmentDao()

    // User operations
    suspend fun insertUser(user: UserEntity): AppResult<Long> {
        return try {
            AppResult.Success(userDao.insertUser(user))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting user", e)
            AppResult.Error("Failed to insert user", e)
        }
    }

    suspend fun updateUser(user: UserEntity): AppResult<Unit> {
        return try {
            AppResult.Success(userDao.updateUser(user))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating user", e)
            AppResult.Error("Failed to update user", e)
        }
    }

    suspend fun deleteUser(user: UserEntity): AppResult<Unit> {
        return try {
            AppResult.Success(userDao.deleteUser(user))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting user", e)
            AppResult.Error("Failed to delete user: ${e.message}", e)
        }
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getUserById(id: Long): Flow<UserEntity?> = userDao.getUserById(id)

    // Program operations
    suspend fun insertProgram(program: PeclProgramEntity): AppResult<Long> {
        return try {
            val existing = programDao.getProgramByName(program.name)
            if (existing != null) {
                return AppResult.Error("Program with name ${program.name} already exists")
            }
            AppResult.Success(programDao.insertProgram(program))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting program", e)
            AppResult.Error("Failed to insert program", e)
        }
    }

    suspend fun updateProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            AppResult.Success(programDao.updateProgram(program))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating program", e)
            AppResult.Error("Failed to update program", e)
        }
    }

    suspend fun deleteProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            db.withTransaction {
                // Check for references (guardrail)
                val pois = poiDao.getPoisForProgramSync(program.id)
                if (pois.isNotEmpty()) {
                    throw IllegalStateException("Cannot delete program - POIs are assigned")
                }
                programDao.deleteProgram(program)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting program", e)
            AppResult.Error(e.message ?: "Failed to delete program", e)
        }
    }

    fun getAllPrograms(): Flow<List<PeclProgramEntity>> = programDao.getAllPrograms()

    fun getProgramById(id: Long): Flow<PeclProgramEntity?> = programDao.getProgramById(id)

    // POI operations (similar pattern with guardrails)
    suspend fun insertPoi(poi: PeclPoiEntity): AppResult<Long> {
        return try {
            if (programDao.getProgramByIdSync(poi.programId) == null) {
                return AppResult.Error("Parent program does not exist")
            }
            AppResult.Success(poiDao.insertPoi(poi))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting POI", e)
            AppResult.Error("Failed to insert POI", e)
        }
    }

    suspend fun updatePoi(poi: PeclPoiEntity): AppResult<Unit> {
        return try {
            AppResult.Success(poiDao.updatePoi(poi))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating POI", e)
            AppResult.Error("Failed to update POI", e)
        }
    }

    suspend fun deletePoi(poi: PeclPoiEntity): AppResult<Unit> {
        return try {
            db.withTransaction {
                val tasks = taskDao.getTasksForPoiSync(poi.id)
                if (tasks.isNotEmpty()) {
                    throw IllegalStateException("Cannot delete POI - tasks are assigned")
                }
                poiDao.deletePoi(poi)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting POI", e)
            AppResult.Error(e.message ?: "Failed to delete POI", e)
        }
    }

    fun getPoisForProgram(programId: Long): Flow<List<PeclPoiEntity>> = poiDao.getPoisForProgram(programId)

    // Task operations (analogous, with guardrails for questions)
    suspend fun insertTask(task: PeclTaskEntity): AppResult<Long> {
        return try {
            if (poiDao.getPoiByIdSync(task.poiId) == null) {
                return AppResult.Error("Parent POI does not exist")
            }
            AppResult.Success(taskDao.insertTask(task))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting task", e)
            AppResult.Error("Failed to insert task", e)
        }
    }

    suspend fun updateTask(task: PeclTaskEntity): AppResult<Unit> {
        return try {
            AppResult.Success(taskDao.updateTask(task))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating task", e)
            AppResult.Error("Failed to update task", e)
        }
    }

    suspend fun deleteTask(task: PeclTaskEntity): AppResult<Unit> {
        return try {
            db.withTransaction {
                val assignments = questionAssignmentDao.getAssignmentsForTaskSync(task.id)
                if (assignments.isNotEmpty()) {
                    throw IllegalStateException("Cannot delete task - questions are assigned")
                }
                taskDao.deleteTask(task)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting task", e)
            AppResult.Error(e.message ?: "Failed to delete task", e)
        }
    }

    fun getTasksForPoi(poiId: Long): Flow<List<PeclTaskEntity>> = taskDao.getTasksForPoi(poiId)

    // Question operations with assignment
    @androidx.room.Transaction
    suspend fun insertQuestionWithAssignment(question: PeclQuestionEntity, taskId: Long): AppResult<Long> {
        return try {
            if (taskDao.getTaskByIdSync(taskId) == null) {
                return AppResult.Error("Parent task does not exist")
            }
            val questionId = questionDao.insertQuestion(question)
            questionAssignmentDao.insertAssignment(PeclQuestionAssignmentEntity(questionId = questionId, taskId = taskId))
            AppResult.Success(questionId)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting question with assignment", e)
            AppResult.Error("Failed to insert question", e)
        }
    }

    suspend fun updateQuestion(question: PeclQuestionEntity): AppResult<Unit> {
        return try {
            AppResult.Success(questionDao.updateQuestion(question))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating question", e)
            AppResult.Error("Failed to update question", e)
        }
    }

    suspend fun deleteQuestion(question: PeclQuestionEntity): AppResult<Unit> {
        return try {
            db.withTransaction {
                val evaluations = evaluationResultDao.getEvaluationsForQuestionSync(question.id)
                if (evaluations.isNotEmpty()) {
                    throw IllegalStateException("Cannot delete question - evaluations exist")
                }
                questionAssignmentDao.deleteAssignmentsForQuestion(question.id)
                questionDao.deleteQuestion(question)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting question", e)
            AppResult.Error(e.message ?: "Failed to delete question", e)
        }
    }

    fun getQuestionsForTask(taskId: Long): Flow<List<PeclQuestionEntity>> = questionDao.getQuestionsForTask(taskId)

    // Scale operations
    suspend fun insertScale(scale: PeclScaleEntity): AppResult<Long> {
        return try {
            AppResult.Success(scaleDao.insertScale(scale))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting scale", e)
            AppResult.Error("Failed to insert scale", e)
        }
    }

    suspend fun updateScale(scale: PeclScaleEntity): AppResult<Unit> {
        return try {
            AppResult.Success(scaleDao.updateScale(scale))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating scale", e)
            AppResult.Error("Failed to update scale", e)
        }
    }

    suspend fun deleteScale(scale: PeclScaleEntity): AppResult<Unit> {
        return try {
            AppResult.Success(scaleDao.deleteScale(scale))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting scale", e)
            AppResult.Error("Failed to delete scale", e)
        }
    }

    fun getAllScales(): Flow<List<PeclScaleEntity>> = scaleDao.getAllScales()

    // Evaluation result operations
    suspend fun insertEvaluationResult(result: PeclEvaluationResultEntity): AppResult<Long> {
        return try {
            // Validation: Check if student, instructor, question exist
            if (userDao.getUserByIdSync(result.studentId) == null ||
                userDao.getUserByIdSync(result.instructorId) == null ||
                questionDao.getQuestionByIdSync(result.questionId) == null
            ) {
                return AppResult.Error("Invalid student, instructor, or question ID")
            }
            AppResult.Success(evaluationResultDao.insertEvaluationResult(result))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting evaluation result", e)
            AppResult.Error("Failed to insert evaluation result", e)
        }
    }

    fun getEvaluationResultsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>> =
        evaluationResultDao.getEvaluationResultsForStudent(studentId)

    // Instructor-Student assignment operations
    suspend fun insertInstructorStudentAssignment(assignment: InstructorStudentAssignmentEntity): AppResult<Long> {
        return try {
            // Validation: Check roles
            val instructor = userDao.getUserByIdSync(assignment.instructorId)
            val student = userDao.getUserByIdSync(assignment.studentId)
            if (instructor?.role != "instructor" || student?.role != "student") {
                return AppResult.Error("Invalid roles for assignment")
            }
            AppResult.Success(instructorStudentAssignmentDao.insertAssignment(assignment))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting instructor-student assignment", e)
            AppResult.Error("Failed to insert assignment", e)
        }
    }

    fun getStudentsForInstructor(instructorId: Long): Flow<List<UserEntity>> =
        instructorStudentAssignmentDao.getStudentsForInstructor(instructorId)

    // Prepopulation (hierarchical insert with ID mapping to avoid duplicates)
    suspend fun prepopulateData() {
        db.withTransaction {
            // Programs
            val programMap = mutableMapOf<String, Long>()
            listOf(
                PeclProgramEntity(name = "AASB"),
                PeclProgramEntity(name = "RSLC")
            ).forEach { program ->
                val id = programDao.insertProgram(program)
                programMap[program.name] = id
            }

            // POIs
            val poiMap = mutableMapOf<String, Long>()
            listOf(
                PeclPoiEntity(name = "AASB_POI_1", programId = programMap["AASB"]!!),
                PeclPoiEntity(name = "RSLC_POI_1", programId = programMap["RSLC"]!!)
            ).forEach { poi ->
                val id = poiDao.insertPoi(poi)
                poiMap[poi.name] = id
            }

            // Tasks
            val taskMap = mutableMapOf<String, Long>()
            listOf(
                PeclTaskEntity(name = "Task1", poiId = poiMap["AASB_POI_1"]!!),
                PeclTaskEntity(name = "Task2", poiId = poiMap["RSLC_POI_1"]!!)
            ).forEach { task ->
                val id = taskDao.insertTask(task)
                taskMap[task.name] = id
            }

            // Questions and assignments (parse from legacy examples)
            // Example: Add your hardcoded questions here, assigning to tasks
            val question1 = PeclQuestionEntity(
                subTask = "Subtask1",
                controlType = "textbox",
                scale = "1-5",
                criticalTask = "yes"
            )
            val q1Id = questionDao.insertQuestion(question1)
            questionAssignmentDao.insertAssignment(PeclQuestionAssignmentEntity(questionId = q1Id, taskId = taskMap["Task1"]!!))

            // Scales
            listOf(
                PeclScaleEntity(scale = "1-10", description = "Numeric scale")
            ).forEach { scale ->
                scaleDao.insertScale(scale)
            }

            // Users (examples)
            listOf(
                UserEntity(name = "Instructor1", role = "instructor"),
                UserEntity(name = "Student1", role = "student")
            ).forEach { user ->
                userDao.insertUser(user)
            }

            // Add more as per your initial prepop data
        }
    }
}