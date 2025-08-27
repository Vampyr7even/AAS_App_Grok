package com.example.aas_app.data

import android.util.Log
import androidx.room.withTransaction
import com.example.aas_app.data.dao.EvaluationResultDao
import com.example.aas_app.data.dao.InstructorProgramAssignmentDao
import com.example.aas_app.data.dao.InstructorStudentAssignmentDao
import com.example.aas_app.data.dao.PeclPoiDao
import com.example.aas_app.data.dao.PeclProgramDao
import com.example.aas_app.data.dao.PeclQuestionDao
import com.example.aas_app.data.dao.PeclStudentDao
import com.example.aas_app.data.dao.PeclTaskDao
import com.example.aas_app.data.dao.PoiProgramAssignmentDao
import com.example.aas_app.data.dao.QuestionAssignmentDao
import com.example.aas_app.data.dao.ScaleDao
import com.example.aas_app.data.dao.TaskPoiAssignmentDao
import com.example.aas_app.data.dao.UserDao
import com.example.aas_app.data.entity.InstructorProgramAssignmentEntity
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.PoiProgramAssignmentEntity
import com.example.aas_app.data.entity.QuestionAssignmentEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.TaskPoiAssignmentEntity
import com.example.aas_app.data.entity.UserEntity
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

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
    private val instructorProgramAssignmentDao = db.instructorProgramAssignmentDao()
    private val evaluationResultDao = db.evaluationResultDao()
    private val scaleDao = db.scaleDao()
    private val poiProgramAssignmentDao = db.poiProgramAssignmentDao()
    private val taskPoiAssignmentDao = db.taskPoiAssignmentDao()
    private val peclStudentDao = db.peclStudentDao()

    // Prepopulation method with transaction and error handling
    suspend fun prePopulateAll(): AppResult<Unit> {
        return try {
            db.withTransaction {
                // Programs
                val programMap = mutableMapOf<String, Long>()
                val programs = listOf("AASB", "RSLC", "USMC Fires", "USMC PFT_CFT")
                programs.forEach { name ->
                    val program = peclProgramDao.getProgramByName(name)
                    if (program == null) {
                        val id = peclProgramDao.insertProgram(PeclProgramEntity(name = name))
                        programMap[name] = id
                        Log.d("AppRepository", "Inserted program: $name with ID: $id")
                    } else {
                        programMap[name] = program.id
                        Log.d("AppRepository", "Existing program: $name with ID: ${program.id}")
                    }
                }
                // POIs
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
                            Log.d("AppRepository", "Assigned POI: $name to program ID: $programId")
                        }
                        Log.d("AppRepository", "Inserted POI: $name with ID: $id")
                        id
                    } else {
                        Log.d("AppRepository", "Existing POI: $name with ID: ${poi.id}")
                        poi.id
                    }
                    poiMap[name] = poiId
                }
                // Tasks
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
                                Log.d("AppRepository", "Assigned task: $name to POI ID: $poiId")
                            }
                            Log.d("AppRepository", "Inserted task: $name with ID: $id")
                            id
                        } else {
                            Log.d("AppRepository", "Existing task: $name with ID: ${task.id}")
                            task.id
                        }
                        taskMap[name] = taskId
                    } else {
                        Log.w("AppRepository", "Skipping task '$name': No valid POIs found for '$poiNamesStr'")
                    }
                }
                // Scales
                val scaleData = listOf(
                    ScaleEntity(scaleName = "1-10", options = "1,2,3,4,5,6,7,8,9,10"),
                    ScaleEntity(scaleName = "Scale_Instructors", options = ""),
                    ScaleEntity(scaleName = "Scale_Comment", options = ""),
                    ScaleEntity(scaleName = "Scale_PECL", options = "1-10"),
                    ScaleEntity(scaleName = "Scale_Yes_No", options = "Yes,No"),
                    ScaleEntity(scaleName = "Scale_Go_NOGO", options = "Go,No Go")
                )
                scaleData.forEach { scale ->
                    val existing = scaleDao.getScaleByName(scale.scaleName)
                    if (existing == null) {
                        scaleDao.insertScale(scale)
                        Log.d("AppRepository", "Inserted scale: ${scale.scaleName}")
                    } else {
                        Log.d("AppRepository", "Existing scale: ${scale.scaleName}")
                    }
                }
                // Questions
                val questionData = listOf(
                    PeclQuestionEntity(subTask = "Instructor Name", controlType = "ComboBox", scale = "Scale_Instructors", criticalTask = "NO") to "Overview",
                    PeclQuestionEntity(subTask = "Student Name", controlType = "ComboBox", scale = "Scale_Students", criticalTask = "NO") to "Overview",
                    PeclQuestionEntity(subTask = "Date", controlType = "Date", scale = "", criticalTask = "NO") to "Overview",
                    PeclQuestionEntity(subTask = "Class", controlType = "TextBox", scale = "", criticalTask = "NO") to "Overview",
                    PeclQuestionEntity(subTask = "Boat Number", controlType = "TextBox", scale = "", criticalTask = "NO") to "Overview",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Launch",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Moor",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Radar-Nav-FLIR",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Plotting",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Radio",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Depart Dock",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Maneuvering the AASB",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "M-O-B",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Maintain Station",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Recover AASB",
                    PeclQuestionEntity(subTask = "Boat Captain", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Comments",
                    PeclQuestionEntity(subTask = "Team Leader", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Confirmation Brief",
                    PeclQuestionEntity(subTask = "Team Leader", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Issue a Warning Order",
                    PeclQuestionEntity(subTask = "Team Leader", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Mission Analysis/IPB",
                    PeclQuestionEntity(subTask = "Team Leader", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Conduct Mission Analysis Brief",
                    PeclQuestionEntity(subTask = "Team Leader", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Develop Teams Course of Action",
                    PeclQuestionEntity(subTask = "Team Leader", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Issue an Operations Order",
                    PeclQuestionEntity(subTask = "Team Leader", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Conduct Rehearsals",
                    PeclQuestionEntity(subTask = "Team Leader", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Conduct Backbrief",
                    PeclQuestionEntity(subTask = "ATL", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Conduct Initial Inspections",
                    PeclQuestionEntity(subTask = "ATL", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Prepare for Mission",
                    PeclQuestionEntity(subTask = "ATL", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Prepare and Issue an OPORD",
                    PeclQuestionEntity(subTask = "ATL", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Issues 4 Para OPORD",
                    PeclQuestionEntity(subTask = "ATL", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Conduct Final Inspection",
                    PeclQuestionEntity(subTask = "RTO", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Prepare for OPORD and Operations",
                    PeclQuestionEntity(subTask = "RTO", controlType = "CheckBox", scale = "Scale_Yes_No", criticalTask = "NO") to "Issue 5 para OPORD",
                    PeclQuestionEntity(subTask = "Fire Support Marine Artillery", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "YES") to "Evaluation Data",
                    PeclQuestionEntity(subTask = "Fire Support Marine Artillery", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "YES") to "Leadership",
                    PeclQuestionEntity(subTask = "Fire Support Marine Artillery", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "YES") to "Preexecution",
                    PeclQuestionEntity(subTask = "Fire Support Marine Artillery", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "YES") to "Call for Fire",
                    PeclQuestionEntity(subTask = "Fire Support Marine Artillery", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "YES") to "Spottings/Corrections",
                    PeclQuestionEntity(subTask = "Fire Support Marine Artillery", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "YES") to "RREMS",
                    PeclQuestionEntity(subTask = "PFTCFT", controlType = "TextBox", scale = "", criticalTask = "NO") to "Idividual Data",
                    PeclQuestionEntity(subTask = "PFTCFT", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "YES") to "PFT Performance Data",
                    PeclQuestionEntity(subTask = "PFTCFT", controlType = "ScoreBox", scale = "Scale_PECL", criticalTask = "YES") to "CFT Performance Data"
                )
                questionData.forEach { (question, taskName) ->
                    val taskId = taskMap[taskName]
                    if (taskId != null) {
                        val existing = peclQuestionDao.getQuestionBySubTask(question.subTask)
                        if (existing == null) {
                            val id = peclQuestionDao.insertQuestion(question.copy(task_id = taskId))
                            questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = id, task_id = taskId))
                            Log.d("AppRepository", "Inserted question: ${question.subTask} with ID: $id assigned to task: $taskName")
                        } else {
                            Log.d("AppRepository", "Existing question: ${question.subTask} with ID: ${existing.id}")
                        }
                    } else {
                        Log.w("AppRepository", "Skipping question '${question.subTask}': No valid task found for '$taskName'")
                    }
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Prepopulation error: ${e.message}", e)
            AppResult.Error("Prepopulation failed: ${e.message}", e)
        }
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getUsersByRole(role: String): Flow<List<UserEntity>> = userDao.getUsersByRole(role)

    suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)

    fun getAllPrograms(): Flow<List<PeclProgramEntity>> = peclProgramDao.getAllPrograms()

    suspend fun getProgramById(id: Long): PeclProgramEntity? = peclProgramDao.getProgramById(id)

    suspend fun getProgramByName(name: String): PeclProgramEntity? = peclProgramDao.getProgramByName(name)

    suspend fun insertProgram(program: PeclProgramEntity): AppResult<Long> {
        return try {
            val id = peclProgramDao.insertProgram(program)
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting program: ${e.message}", e)
        }
    }

    suspend fun updateProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            peclProgramDao.updateProgram(program)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error updating program: ${e.message}", e)
        }
    }

    suspend fun deleteProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            peclProgramDao.deleteProgram(program)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting program: ${e.message}", e)
        }
    }

    fun getPoisForProgram(programId: Long): Flow<List<PeclPoiEntity>> = peclPoiDao.getPoisForProgram(programId)

    fun getAllPois(): Flow<List<PeclPoiEntity>> = peclPoiDao.getAllPois()

    suspend fun getPoiById(id: Long): PeclPoiEntity? = peclPoiDao.getPoiById(id)

    suspend fun getPoiByName(name: String): PeclPoiEntity? = peclPoiDao.getPoiByName(name)

    suspend fun insertPoi(poi: PeclPoiEntity, programIds: List<Long>): AppResult<Long> {
        return try {
            val id = db.withTransaction {
                val insertedId = peclPoiDao.insertPoi(poi)
                programIds.forEach { programId ->
                    poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = insertedId, program_id = programId))
                }
                insertedId
            }
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting POI: ${e.message}", e)
        }
    }

    suspend fun updatePoi(poi: PeclPoiEntity, programIds: List<Long>): AppResult<Unit> {
        return try {
            db.withTransaction {
                peclPoiDao.updatePoi(poi)
                poiProgramAssignmentDao.deleteAssignmentsForPoi(poi.id)
                programIds.forEach { programId ->
                    poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = poi.id, program_id = programId))
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error updating POI: ${e.message}", e)
        }
    }

    suspend fun deletePoi(poi: PeclPoiEntity): AppResult<Unit> {
        return try {
            db.withTransaction {
                peclPoiDao.deletePoi(poi)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting POI: ${e.message}", e)
        }
    }

    fun getTasksForPoi(poiId: Long): Flow<List<PeclTaskEntity>> = peclTaskDao.getTasksForPoi(poiId)

    fun getAllTasks(): Flow<List<PeclTaskEntity>> = peclTaskDao.getAllTasks()

    suspend fun getTaskById(id: Long): PeclTaskEntity? = peclTaskDao.getTaskById(id)

    suspend fun getTaskByName(name: String): PeclTaskEntity? = peclTaskDao.getTaskByName(name)

    suspend fun insertTask(task: PeclTaskEntity, poiIds: List<Long>): AppResult<Long> {
        return try {
            val id = db.withTransaction {
                val insertedId = peclTaskDao.insertTask(task)
                poiIds.forEach { poiId ->
                    taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = insertedId, poi_id = poiId))
                }
                insertedId
            }
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting task: ${e.message}", e)
        }
    }

    suspend fun updateTask(task: PeclTaskEntity, poiIds: List<Long>?): AppResult<Unit> {
        return try {
            db.withTransaction {
                peclTaskDao.updateTask(task)
                if (poiIds != null) {
                    taskPoiAssignmentDao.deleteAssignmentsForTask(task.id)
                    poiIds.forEach { poiId ->
                        taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = task.id, poi_id = poiId))
                    }
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error updating task: ${e.message}", e)
        }
    }

    suspend fun deleteTask(task: PeclTaskEntity): AppResult<Unit> {
        return try {
            db.withTransaction {
                peclTaskDao.deleteTask(task)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting task: ${e.message}", e)
        }
    }

    fun getAllQuestions(): Flow<List<PeclQuestionEntity>> = peclQuestionDao.getAllQuestions()

    fun getQuestionsForTask(taskId: Long): Flow<List<PeclQuestionEntity>> = peclQuestionDao.getQuestionsForTask(taskId)

    fun getQuestionsForPoi(poiId: Long): Flow<List<PeclQuestionEntity>> = peclQuestionDao.getQuestionsForPoi(poiId)

    suspend fun getQuestionById(id: Long): PeclQuestionEntity? = peclQuestionDao.getQuestionById(id)

    suspend fun insertQuestion(question: PeclQuestionEntity, taskId: Long): AppResult<Long> {
        return try {
            val id = db.withTransaction {
                val insertedId = peclQuestionDao.insertQuestion(question)
                questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = insertedId, task_id = taskId))
                insertedId
            }
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting question: ${e.message}", e)
        }
    }

    suspend fun updateQuestion(question: PeclQuestionEntity, taskId: Long): AppResult<Unit> {
        return try {
            db.withTransaction {
                peclQuestionDao.updateQuestion(question)
                // If changing task, update assignment
                questionAssignmentDao.deleteAssignmentsForQuestion(question.id)
                questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = question.id, task_id = taskId))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error updating question: ${e.message}", e)
        }
    }

    suspend fun deleteQuestion(question: PeclQuestionEntity): AppResult<Unit> {
        return try {
            db.withTransaction {
                questionAssignmentDao.deleteAssignmentsForQuestion(question.id)
                peclQuestionDao.deleteQuestion(question)
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting question: ${e.message}", e)
        }
    }

    fun getAllScales(): Flow<List<ScaleEntity>> = scaleDao.getAllScales()

    suspend fun getScaleById(id: Long): ScaleEntity? = scaleDao.getScaleById(id)

    suspend fun getScaleByName(name: String): ScaleEntity? = scaleDao.getScaleByName(name)

    suspend fun insertScale(scale: ScaleEntity): AppResult<Long> {
        return try {
            val id = scaleDao.insertScale(scale)
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting scale: ${e.message}", e)
        }
    }

    suspend fun updateScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            scaleDao.updateScale(scale)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error updating scale: ${e.message}", e)
        }
    }

    suspend fun deleteScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            scaleDao.deleteScale(scale)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting scale: ${e.message}", e)
        }
    }

    fun getStudentsForInstructor(instructorId: Long): Flow<List<PeclStudentEntity>> = instructorStudentAssignmentDao.getStudentsForInstructor(instructorId)

    fun getStudentsForProgram(programId: Long): Flow<List<PeclStudentEntity>> = instructorStudentAssignmentDao.getStudentsForProgram(programId)

    fun getAssignmentsForInstructor(instructorId: Long): Flow<List<InstructorStudentAssignmentEntity>> = instructorStudentAssignmentDao.getAssignmentsForInstructor(instructorId)

    suspend fun insertAssignment(assignment: InstructorStudentAssignmentEntity): AppResult<Long> {
        return try {
            val id = instructorStudentAssignmentDao.insertAssignment(assignment)
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting assignment: ${e.message}", e)
        }
    }

    suspend fun deleteAssignmentsForInstructor(instructorId: Long): AppResult<Unit> {
        return try {
            instructorStudentAssignmentDao.deleteAssignmentsForInstructor(instructorId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting assignments: ${e.message}", e)
        }
    }

    suspend fun insertInstructorProgramAssignment(assignment: InstructorProgramAssignmentEntity): AppResult<Long> {
        return try {
            val id = instructorProgramAssignmentDao.insertAssignment(assignment)
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting instructor program assignment: ${e.message}", e)
        }
    }

    suspend fun deleteInstructorProgramAssignmentsForInstructor(instructorId: Long): AppResult<Unit> {
        return try {
            instructorProgramAssignmentDao.deleteAssignmentsForInstructor(instructorId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting instructor program assignments: ${e.message}", e)
        }
    }

    fun getProgramsForInstructor(instructorId: Long): Flow<List<String>> = instructorProgramAssignmentDao.getProgramsForInstructor(instructorId)

    fun getEvaluationResultsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>> = evaluationResultDao.getEvaluationResultsForStudent(studentId)

    suspend fun getAverageScoreForStudent(studentId: Long, poiId: Long): Double = evaluationResultDao.getAverageScoreForStudent(studentId, poiId)

    suspend fun insertEvaluationResult(result: PeclEvaluationResultEntity): AppResult<Long> {
        return try {
            val id = evaluationResultDao.insertEvaluationResult(result)
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting evaluation result: ${e.message}", e)
        }
    }

    fun getProgramsForPoi(poiId: Long): Flow<List<String>> = poiProgramAssignmentDao.getProgramsForPoi(poiId)

    fun getPoisForTask(taskId: Long): Flow<List<String>> = taskPoiAssignmentDao.getPoisForTask(taskId)

    fun getAllPeclStudents(): Flow<List<PeclStudentEntity>> = peclStudentDao.getAllStudents()

    suspend fun insertPeclStudent(student: PeclStudentEntity): AppResult<Long> {
        return try {
            val id = peclStudentDao.insertStudent(student)
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting student: ${e.message}", e)
        }
    }

    suspend fun updatePeclStudent(student: PeclStudentEntity): AppResult<Unit> {
        return try {
            peclStudentDao.updateStudent(student)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error updating student: ${e.message}", e)
        }
    }

    suspend fun deletePeclStudent(student: PeclStudentEntity): AppResult<Unit> {
        return try {
            peclStudentDao.deleteStudent(student)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting student: ${e.message}", e)
        }
    }

    fun getPeclStudentById(id: Long): Flow<PeclStudentEntity?> = peclStudentDao.getStudentById(id)
}