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

    // Prepopulation method
    suspend fun prePopulateAll() {
        db.withTransaction {
            // Clear existing data if needed for dev
            // Programs
            val programMap = mutableMapOf<String, Long>()
            val programs = listOf("AASB", "RSLC")
            programs.forEach { name ->
                var program = peclProgramDao.getProgramByName(name)
                if (program == null) {
                    val id = peclProgramDao.insertProgram(PeclProgramEntity(peclProgram = name))
                    programMap[name] = id
                } else {
                    programMap[name] = program.id.toLong()
                }
            }

            // POIs
            val poiMap = mutableMapOf<String, Long>()
            listOf(
                Pair("AASB_POI_1", programMap["AASB"]!!),
                Pair("RSLC_POI_1", programMap["RSLC"]!!)
            ).forEach { (name, programId) ->
                var poi = peclPoiDao.getPoiByName(name)
                if (poi == null) {
                    val id = peclPoiDao.insertPoi(PeclPoiEntity(name = name, program_id = programId.toInt()))
                    poiMap[name] = id
                } else {
                    poiMap[name] = poi.id.toLong()
                }
            }

            // Tasks
            val taskMap = mutableMapOf<String, Long>()
            listOf(
                Pair("Task1", poiMap["AASB_POI_1"]!!),
                Pair("Task2", poiMap["RSLC_POI_1"]!!)
            ).forEach { (name, poiId) ->
                var task = peclTaskDao.getTaskByName(name)
                if (task == null) {
                    val id = peclTaskDao.insertTask(PeclTaskEntity(name = name, poi_id = poiId.toInt()))
                    taskMap[name] = id
                } else {
                    taskMap[name] = task.id.toLong()
                }
            }

            // Questions
            val questions = listOf(
                PeclQuestionEntity(subTask = "Subtask1", controlType = "textbox", scale = "1-5", criticalTask = "yes")
            )
            questions.forEach { question ->
                val qId = peclQuestionDao.insertQuestion(question)
                questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = qId.toInt(), task_id = taskMap["Task1"]!!.toInt()))
            }

            // Scales
            listOf(
                ScaleEntity(scaleName = "1-10", scaleData = "1,2,3,4,5,6,7,8,9,10")
            ).forEach { scale ->
                scaleDao.insertScale(scale)
            }

            // Users and assignments
            val instructorId = userDao.insertUser(UserEntity(firstName = "Instructor1", lastName = "", grade = "", pin = null, fullName = "Instructor1", assignedProject = null, role = "instructor"))
            val studentId = userDao.insertUser(UserEntity(firstName = "Student1", lastName = "", grade = "", pin = null, fullName = "Student1", assignedProject = null, role = "student"))
            instructorStudentAssignmentDao.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructorId.toInt(), student_id = studentId.toInt(), program_id = programMap["AASB"]!!.toInt()))
        }
    }

    // User methods
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

    fun getUsersByRole(role: String): Flow<List<UserEntity>> = userDao.getUsersByRole(role)

    // Similar methods for other entities, with guardrails and transactions as in previous outline
    // (Omitted for brevity, but implement similarly)
}