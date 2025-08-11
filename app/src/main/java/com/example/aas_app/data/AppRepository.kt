package com.example.aas_app.data

import android.util.Log
import androidx.room.withTransaction
import com.example.aas_app.data.dao.EvaluationResultDao
import com.example.aas_app.data.dao.InstructorStudentAssignmentDao
import com.example.aas_app.data.dao.PeclPoiDao
import com.example.aas_app.data.dao.PeclProgramDao
import com.example.aas_app.data.dao.PeclQuestionDao
import com.example.aas_app.data.dao.PeclTaskDao
import com.example.aas_app.data.dao.QuestionAssignmentDao
import com.example.aas_app.data.dao.ScaleDao
import com.example.aas_app.data.dao.UserDao
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.UserEntity
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : AppResult<Nothing>()
}

@ViewModelScoped
class AppRepository @Inject constructor(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val peclProgramDao = db.peclProgramDao()
    private val peclPoiDao = db.peclPoiDao()
    private val peclTaskDao = db.peclTaskDao()
    private val peclQuestionDao = db.peclQuestionDao()
    private val questionAssignmentDao = db.questionAssignmentDao()
    private val instructorStudentAssignmentDao = db.instructorStudentAssignmentDao()
    private val evaluationResultDao = db.evaluationResultDao()
    private val scaleDao = db.scaleDao()

    // Prepopulation method with transaction and error handling
    suspend fun prePopulateAll(): AppResult<Unit> {
        return try {
            db.withTransaction {
                // Clear existing data if needed for dev (comment out in production)
                // Programs
                val programMap = mutableMapOf<String, Long>()
                val programs = listOf("AASB", "RSLC")
                programs.forEach { name ->
                    val program = peclProgramDao.getProgramByName(name)
                    if (program == null) {
                        val id = peclProgramDao.insertProgram(PeclProgramEntity(name = name))
                        programMap[name] = id
                    } else {
                        programMap[name] = program.id
                    }
                }
                // POIs
                val poiMap = mutableMapOf<String, Long>()
                listOf(
                    Pair("AASB_POI_1", programMap["AASB"]!!),
                    Pair("RSLC_POI_1", programMap["RSLC"]!!)
                ).forEach { (name, programId) ->
                    val poi = peclPoiDao.getPoiByName(name)
                    if (poi == null) {
                        val id = peclPoiDao.insertPoi(PeclPoiEntity(name = name, program_id = programId))
                        poiMap[name] = id
                    } else {
                        poiMap[name] = poi.id
                    }
                }
                // Tasks
                val taskMap = mutableMapOf<String, Long>()
                listOf(
                    Pair("Task1", poiMap["AASB_POI_1"]!!),
                    Pair("Task2", poiMap["RSLC_POI_1"]!!)
                ).forEach { (name, poiId) ->
                    val task = peclTaskDao.getTaskByName(name)
                    if (task == null) {
                        val id = peclTaskDao.insertTask(PeclTaskEntity(name = name, poi_id = poiId))
                        taskMap[name] = id
                    } else {
                        taskMap[name] = task.id
                    }
                }
                // Questions
                val questions = listOf(
                    PeclQuestionEntity(subTask = "Subtask1", controlType = "textbox", scale = "1-5", criticalTask = "yes")
                )
                questions.forEach { question ->
                    val qId = peclQuestionDao.insertQuestion(question)
                    questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = qId, task_id = taskMap["Task1"]!!))
                }
                // Scales
                listOf(
                    ScaleEntity(scaleName = "1-10", options = "1,2,3,4,5,6,7,8,9,10")
                ).forEach { scale ->
                    scaleDao.insertScale(scale)
                }
                // Users and assignments
                val instructorId = userDao.insertUser(UserEntity(firstName = "Instructor1", lastName = "", grade = "", pin = null, fullName = "Instructor1", assignedProject = null, role = "instructor"))
                val studentId = userDao.insertUser(UserEntity(firstName = "Student1", lastName = "", grade = "", pin = null, fullName = "Student1", assignedProject = null, role = "student"))
                instructorStudentAssignmentDao.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructorId, student_id = studentId, program_id = programMap["AASB"]!!))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error during prepopulation", e)
            AppResult.Error("Prepopulation failed: ${e.message}", e)
        }
    }

    // User methods
    suspend fun insertUser(user: UserEntity): AppResult<Long> {
        return try {
            AppResult.Success(userDao.insertUser(user))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting user", e)
            AppResult.Error("Failed to insert user: ${e.message}", e)
        }
    }

    suspend fun updateUser(user: UserEntity): AppResult<Unit> {
        return try {
            AppResult.Success(userDao.updateUser(user))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating user", e)
            AppResult.Error("Failed to update user: ${e.message}", e)
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

    fun getUsersByRole(role: String): Flow<List<UserEntity>> = userDao.getUsersByRole(role)

    suspend fun insertProgram(program: PeclProgramEntity): AppResult<Long> {
        return try {
            AppResult.Success(peclProgramDao.insertProgram(program))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting program", e)
            AppResult.Error("Failed to insert program: ${e.message}", e)
        }
    }

    suspend fun updateProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            AppResult.Success(peclProgramDao.updateProgram(program))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating program", e)
            AppResult.Error("Failed to update program: ${e.message}", e)
        }
    }

    suspend fun deleteProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            AppResult.Success(peclProgramDao.deleteProgram(program))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting program", e)
            AppResult.Error("Failed to delete program: ${e.message}", e)
        }
    }

    fun getAllPrograms(): Flow<List<PeclProgramEntity>> = peclProgramDao.getAllPrograms()

    suspend fun getProgramById(id: Long): PeclProgramEntity? = peclProgramDao.getProgramById(id)

    suspend fun getProgramByName(name: String): PeclProgramEntity? = peclProgramDao.getProgramByName(name)

    suspend fun insertPoi(poi: PeclPoiEntity): AppResult<Long> {
        return try {
            AppResult.Success(peclPoiDao.insertPoi(poi))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting POI", e)
            AppResult.Error("Failed to insert POI: ${e.message}", e)
        }
    }

    suspend fun updatePoi(poi: PeclPoiEntity): AppResult<Unit> {
        return try {
            AppResult.Success(peclPoiDao.updatePoi(poi))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating POI", e)
            AppResult.Error("Failed to update POI: ${e.message}", e)
        }
    }

    suspend fun deletePoi(poi: PeclPoiEntity): AppResult<Unit> {
        return try {
            AppResult.Success(peclPoiDao.deletePoi(poi))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting POI", e)
            AppResult.Error("Failed to delete POI: ${e.message}", e)
        }
    }

    fun getPoisForProgram(programId: Long): Flow<List<PeclPoiEntity>> = peclPoiDao.getPoisForProgram(programId)

    suspend fun getPoiById(id: Long): PeclPoiEntity? = peclPoiDao.getPoiById(id)

    suspend fun getPoiByName(name: String): PeclPoiEntity? = peclPoiDao.getPoiByName(name)

    suspend fun insertTask(task: PeclTaskEntity): AppResult<Long> {
        return try {
            AppResult.Success(peclTaskDao.insertTask(task))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting task", e)
            AppResult.Error("Failed to insert task: ${e.message}", e)
        }
    }

    suspend fun updateTask(task: PeclTaskEntity): AppResult<Unit> {
        return try {
            AppResult.Success(peclTaskDao.updateTask(task))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating task", e)
            AppResult.Error("Failed to update task: ${e.message}", e)
        }
    }

    suspend fun deleteTask(task: PeclTaskEntity): AppResult<Unit> {
        return try {
            AppResult.Success(peclTaskDao.deleteTask(task))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting task", e)
            AppResult.Error("Failed to delete task: ${e.message}", e)
        }
    }

    fun getTasksForPoi(poiId: Long): Flow<List<PeclTaskEntity>> = peclTaskDao.getTasksForPoi(poiId)

    suspend fun getTaskById(id: Long): PeclTaskEntity? = peclTaskDao.getTaskById(id)

    suspend fun getTaskByName(name: String): PeclTaskEntity? = peclTaskDao.getTaskByName(name)

    suspend fun insertQuestion(question: PeclQuestionEntity): AppResult<Long> {
        return try {
            AppResult.Success(peclQuestionDao.insertQuestion(question))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting question", e)
            AppResult.Error("Failed to insert question: ${e.message}", e)
        }
    }

    suspend fun updateQuestion(question: PeclQuestionEntity): AppResult<Unit> {
        return try {
            AppResult.Success(peclQuestionDao.updateQuestion(question))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating question", e)
            AppResult.Error("Failed to update question: ${e.message}", e)
        }
    }

    suspend fun deleteQuestion(question: PeclQuestionEntity): AppResult<Unit> {
        return try {
            AppResult.Success(peclQuestionDao.deleteQuestion(question))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting question", e)
            AppResult.Error("Failed to delete question: ${e.message}", e)
        }
    }

    fun getAllQuestions(): Flow<List<PeclQuestionEntity>> = peclQuestionDao.getAllQuestions()

    fun getQuestionsForPoi(program: String, poi: String): Flow<List<PeclQuestionEntity>> = peclQuestionDao.getQuestionsForPoi(program, poi)

    suspend fun getQuestionById(id: Long): PeclQuestionEntity? = peclQuestionDao.getQuestionById(id)

    suspend fun insertQuestionWithAssignment(question: PeclQuestionEntity, taskId: Long): AppResult<Unit> {
        return try {
            db.withTransaction {
                val qId = peclQuestionDao.insertQuestion(question)
                questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = qId, task_id = taskId))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting question with assignment", e)
            AppResult.Error("Failed to insert question with assignment: ${e.message}", e)
        }
    }

    fun getQuestionsForTask(taskId: Long): Flow<List<PeclQuestionEntity>> = peclQuestionDao.getQuestionsForTask(taskId)

    suspend fun insertScale(scale: ScaleEntity): AppResult<Long> {
        return try {
            AppResult.Success(scaleDao.insertScale(scale))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting scale", e)
            AppResult.Error("Failed to insert scale: ${e.message}", e)
        }
    }

    suspend fun updateScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            AppResult.Success(scaleDao.updateScale(scale))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating scale", e)
            AppResult.Error("Failed to update scale: ${e.message}", e)
        }
    }

    suspend fun deleteScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            AppResult.Success(scaleDao.deleteScale(scale))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting scale", e)
            AppResult.Error("Failed to delete scale: ${e.message}", e)
        }
    }

    fun getAllScales(): Flow<List<ScaleEntity>> = scaleDao.getAllScales()

    suspend fun getScaleById(id: Long): ScaleEntity? = scaleDao.getScaleById(id) // Add DAO method

    suspend fun insertEvaluationResult(result: PeclEvaluationResultEntity): AppResult<Long> {
        return try {
            AppResult.Success(evaluationResultDao.insertEvaluationResult(result))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting evaluation result", e)
            AppResult.Error("Failed to insert evaluation result: ${e.message}", e)
        }
    }

    fun getEvaluationResultsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>> = evaluationResultDao.getEvaluationResultsForStudent(studentId)

    suspend fun getAverageScoreForStudent(studentId: Long, poiId: Long): Float = evaluationResultDao.getAverageScoreForStudent(studentId, poiId).toFloat()

    suspend fun getEvaluationsForQuestion(questionId: Long): List<PeclEvaluationResultEntity> = evaluationResultDao.getEvaluationsForQuestion(questionId)

    suspend fun insertAssignment(assignment: InstructorStudentAssignmentEntity): AppResult<Long> {
        return try {
            AppResult.Success(instructorStudentAssignmentDao.insertAssignment(assignment))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting assignment", e)
            AppResult.Error("Failed to insert assignment: ${e.message}", e)
        }
    }

    fun getStudentsForInstructor(instructorId: Long): Flow<List<UserEntity>> = instructorStudentAssignmentDao.getStudentsForInstructor(instructorId)

    fun getStudentsForProgram(programId: Long): Flow<List<UserEntity>> = instructorStudentAssignmentDao.getStudentsForProgram(programId)

    fun getQuestionsForPoi(poiId: Long): Flow<List<PeclQuestionEntity>> = peclQuestionDao.getQuestionsForPoi(poiId) // Add DAO method for POI-specific questions

    // Add other missing methods similarly
}