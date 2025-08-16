package com.example.aas_app.data

import android.util.Log
import androidx.room.withTransaction
import com.example.aas_app.data.dao.EvaluationResultDao
import com.example.aas_app.data.dao.InstructorStudentAssignmentDao
import com.example.aas_app.data.dao.PeclPoiDao
import com.example.aas_app.data.dao.PeclProgramDao
import com.example.aas_app.data.dao.PeclQuestionDao
import com.example.aas_app.data.dao.PeclTaskDao
import com.example.aas_app.data.dao.PoiProgramAssignmentDao
import com.example.aas_app.data.dao.QuestionAssignmentDao
import com.example.aas_app.data.dao.ScaleDao
import com.example.aas_app.data.dao.TaskPoiAssignmentDao
import com.example.aas_app.data.dao.UserDao
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.PoiProgramAssignmentEntity
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.TaskPoiAssignmentEntity
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
    private val poiProgramAssignmentDao = db.poiProgramAssignmentDao()
    private val taskPoiAssignmentDao = db.taskPoiAssignmentDao()

    // Prepopulation method with transaction and error handling
    suspend fun prePopulateAll(): AppResult<Unit> {
        return try {
            db.withTransaction {
                // Clear existing data if needed for dev (comment out in production)
                // Programs
                val programMap = mutableMapOf<String, Long>()
                val programs = listOf("AASB", "RSLC", "USMC Fires", "USMC PFT_CFT")
                programs.forEach { name ->
                    val program = peclProgramDao.getProgramByName(name)
                    if (program == null) {
                        val id = peclProgramDao.insertProgram(PeclProgramEntity(name = name))
                        programMap[name] = id
                    } else {
                        programMap[name] = program.id
                    }
                }
                // POIs - Updated with user's provided data, assigned to programs
                val poiMap = mutableMapOf<String, Long>()
                val poiAssignments = listOf(
                    "Boat Operations" to listOf(programMap["AASB"]!!),
                    "Team Leader Planning" to listOf(programMap["RSLC"]!!),
                    "ATL Planning" to listOf(programMap["RSLC"]!!),
                    "RTO Planning" to listOf(programMap["RSLC"]!!),
                    "Fire Support Marine Artillery" to listOf(programMap["USMC Fires"]!!),
                    "PFTCFT" to listOf(programMap["USMC PFT_CFT"]!!)
                )
                poiAssignments.forEach { (name, programIds) ->
                    val poi = peclPoiDao.getPoiByName(name)
                    val poiId = if (poi == null) {
                        val id = peclPoiDao.insertPoi(PeclPoiEntity(name = name))
                        programIds.forEach { programId ->
                            poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = id, program_id = programId))
                        }
                        id
                    } else {
                        // For existing, update assignments if needed (assume add if not present)
                        poi.id
                    }
                    poiMap[name] = poiId
                }
                // Tasks - Updated with user's provided data, assigned to multiple POIs
                val taskMap = mutableMapOf<String, Long>()
                val taskData = listOf(
                    "Launch" to "Boat Operations",
                    "Moor" to "Boat Operations",
                    "Radar-Nav-FLIR" to "Boat Operations",
                    "Plotting" to "Boat Operations",
                    "Radio" to "Boat Operations",
                    "Depart Dock" to "Boat Operations",
                    "Maneuvering the AASB" to "Boat Operations",
                    "M-O-B" to "Boat Operations",
                    "Maintain Station" to "Boat Operations",
                    "Recover AASB" to "Boat Operations",
                    "Comments" to "Boat Operations",
                    "Confirmation Brief" to "Team Leader Planning",
                    "Issue a Warning Order" to "Team Leader Planning",
                    "Mission Analysis/IPB" to "Team Leader Planning",
                    "Conduct Mission Analysis Brief" to "Team Leader Planning",
                    "Develop Teams Course of Action" to "Team Leader Planning",
                    "Issue an Operations Order" to "Team Leader Planning",
                    "Conduct Rehearsals" to "Team Leader Planning,ATL Planning,RTO Planning",
                    "Conduct Backbrief" to "Team Leader Planning,ATL Planning,RTO Planning",
                    "Evaluation Data" to "Fire Support Marine Artillery",
                    "Leadership" to "Fire Support Marine Artillery",
                    "Conduct Initial Inspections" to "ATL Planning",
                    "Prepare for Mission" to "ATL Planning",
                    "Prepare and Issue an OPORD" to "ATL Planning",
                    "Issues 4 Para OPORD" to "ATL Planning",
                    "Conduct Final Inspection" to "ATL Planning",
                    "Prepare for OPORD and Operations" to "RTO Planning",
                    "Issue 5 para OPORD" to "RTO Planning",
                    "Preexecution" to "Fire Support Marine Artillery",
                    "Call for Fire" to "Fire Support Marine Artillery",
                    "Spottings/Corrections" to "Fire Support Marine Artillery",
                    "RREMS" to "Fire Support Marine Artillery",
                    "Idividual Data" to "PFTCFT",
                    "PFT Performance Data" to "PFTCFT",
                    "CFT Performance Data" to "PFTCFT"
                )
                taskData.forEach { (name, poiNamesStr) ->
                    val poiNames = poiNamesStr.split(",").map { it.trim() }
                    val poiIds = poiNames.mapNotNull { poiMap[it] }
                    if (poiIds.isNotEmpty()) {
                        val task = peclTaskDao.getTaskByName(name)
                        val taskId = if (task == null) {
                            val id = peclTaskDao.insertTask(PeclTaskEntity(name = name))
                            poiIds.forEach { poiId ->
                                taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = id, poi_id = poiId))
                            }
                            id
                        } else {
                            // For existing, update assignments if needed (assume add if not present)
                            task.id
                        }
                        taskMap[name] = taskId
                    } else {
                        Log.w("AppRepository", "Skipping task '$name': No valid POIs found for '$poiNamesStr'")
                    }
                }
                // Scales
                listOf(
                    ScaleEntity(scaleName = "1-10", options = "1,2,3,4,5,6,7,8,9,10")
                ).forEach { scale ->
                    scaleDao.insertScale(scale)
                }
                // Questions
                val questions = listOf(
                    PeclQuestionEntity(subTask = "Subtask1", controlType = "textbox", scale = "1-5", criticalTask = "yes")
                )
                questions.forEach { question ->
                    val qId = peclQuestionDao.insertQuestion(question)
                    questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = qId, task_id = taskMap["Launch"] ?: 0L))  // Example assignment; adjust as needed
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

    suspend fun insertPoi(poi: PeclPoiEntity, programIds: List<Long>): AppResult<Long> {
        return try {
            if (programIds.isEmpty()) {
                return AppResult.Error("Cannot insert POI: At least one program must be assigned")
            }
            db.withTransaction {
                programIds.forEach { programId ->
                    if (getProgramById(programId) == null) {
                        throw Exception("Cannot insert POI: Program ID $programId does not exist")
                    }
                }
                val poiId = peclPoiDao.insertPoi(poi)
                programIds.forEach { programId ->
                    poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = poiId, program_id = programId))
                }
                AppResult.Success(poiId)
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting POI with assignments", e)
            AppResult.Error("Failed to insert POI: ${e.message}", e)
        }
    }

    suspend fun updatePoi(poi: PeclPoiEntity, programIds: List<Long>): AppResult<Unit> {
        return try {
            if (programIds.isEmpty()) {
                return AppResult.Error("Cannot update POI: At least one program must be assigned")
            }
            db.withTransaction {
                programIds.forEach { programId ->
                    if (getProgramById(programId) == null) {
                        throw Exception("Cannot update POI: Program ID $programId does not exist")
                    }
                }
                peclPoiDao.updatePoi(poi)
                poiProgramAssignmentDao.deleteAssignmentsForPoi(poi.id)
                programIds.forEach { programId ->
                    poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = poi.id, program_id = programId))
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating POI with assignments", e)
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

    fun getAllPois(): Flow<List<PeclPoiEntity>> = peclPoiDao.getAllPois()

    suspend fun getPoiById(id: Long): PeclPoiEntity? = peclPoiDao.getPoiById(id)

    suspend fun getPoiByName(name: String): PeclPoiEntity? = peclPoiDao.getPoiByName(name)

    suspend fun getProgramIdsForPoi(poiId: Long): List<Long> = poiProgramAssignmentDao.getProgramIdsForPoi(poiId)

    fun getProgramsForPoi(poiId: Long): Flow<List<String>> = poiProgramAssignmentDao.getProgramsForPoi(poiId)

    suspend fun insertTask(task: PeclTaskEntity, poiIds: List<Long>): AppResult<Long> {
        return try {
            if (poiIds.isEmpty()) {
                return AppResult.Error("Cannot insert task: At least one POI must be assigned")
            }
            db.withTransaction {
                poiIds.forEach { poiId ->
                    if (getPoiById(poiId) == null) {
                        throw Exception("Cannot insert task: POI ID $poiId does not exist")
                    }
                }
                val taskId = peclTaskDao.insertTask(task)
                poiIds.forEach { poiId ->
                    taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = taskId, poi_id = poiId))
                }
                AppResult.Success(taskId)
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting task with assignments", e)
            AppResult.Error("Failed to insert task: ${e.message}", e)
        }
    }

    suspend fun updateTask(task: PeclTaskEntity, poiIds: List<Long>? = null): AppResult<Unit> {
        return try {
            db.withTransaction {
                peclTaskDao.updateTask(task)
                if (poiIds != null) {
                    if (poiIds.isEmpty()) {
                        return@withTransaction AppResult.Error("Cannot update task: At least one POI must be assigned")
                    }
                    poiIds.forEach { poiId ->
                        if (getPoiById(poiId) == null) {
                            throw Exception("Cannot update task: POI ID $poiId does not exist")
                        }
                    }
                    taskPoiAssignmentDao.deleteAssignmentsForTask(task.id)
                    poiIds.forEach { poiId ->
                        taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = task.id, poi_id = poiId))
                    }
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating task with assignments", e)
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

    fun getAllTasks(): Flow<List<PeclTaskEntity>> = peclTaskDao.getAllTasks()

    suspend fun getTaskById(id: Long): PeclTaskEntity? = peclTaskDao.getTaskById(id)

    suspend fun getTaskByName(name: String): PeclTaskEntity? = peclTaskDao.getTaskByName(name)

    suspend fun getPoiIdsForTask(taskId: Long): List<Long> = taskPoiAssignmentDao.getPoiIdsForTask(taskId)

    fun getPoisForTask(taskId: Long): Flow<List<String>> = taskPoiAssignmentDao.getPoisForTask(taskId)

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
                if (getTaskById(taskId) == null) {
                    throw Exception("Cannot assign question: Task ID $taskId does not exist")
                }
                val qId = peclQuestionDao.insertQuestion(question.copy(task_id = taskId))
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

    suspend fun getScaleById(id: Long): ScaleEntity? = scaleDao.getScaleById(id)

    suspend fun insertEvaluationResult(result: PeclEvaluationResultEntity): AppResult<Long> {
        return try {
            if (getQuestionById(result.question_id) == null) {
                return AppResult.Error("Cannot insert result: Question ID ${result.question_id} does not exist")
            }
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
            if (assignment.program_id != null && getProgramById(assignment.program_id!!) == null) {
                return AppResult.Error("Cannot insert assignment: Program ID ${assignment.program_id} does not exist")
            }
            AppResult.Success(instructorStudentAssignmentDao.insertAssignment(assignment))
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting assignment", e)
            AppResult.Error("Failed to insert assignment: ${e.message}", e)
        }
    }

    fun getStudentsForInstructor(instructorId: Long): Flow<List<UserEntity>> = instructorStudentAssignmentDao.getStudentsForInstructor(instructorId)

    fun getStudentsForProgram(programId: Long): Flow<List<UserEntity>> = instructorStudentAssignmentDao.getStudentsForProgram(programId)

    fun getQuestionsForPoi(poiId: Long): Flow<List<PeclQuestionEntity>> = peclQuestionDao.getQuestionsForPoi(poiId)
    // Add other missing methods similarly
}