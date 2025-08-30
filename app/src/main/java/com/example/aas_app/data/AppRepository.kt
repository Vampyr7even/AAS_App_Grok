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
import com.example.aas_app.data.entity.UserEntity
import com.example.aas_app.viewmodel.QuestionWithTask
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : AppResult<Nothing>()
}

@ViewModelScoped
class AppRepository @Inject constructor(private val db: AppDatabase) {

    private val userDao: UserDao = db.userDao()
    private val peclProgramDao: PeclProgramDao = db.peclProgramDao()
    private val peclPoiDao: PeclPoiDao = db.peclPoiDao()
    private val peclTaskDao: PeclTaskDao = db.peclTaskDao()
    private val peclQuestionDao: PeclQuestionDao = db.peclQuestionDao()
    private val questionAssignmentDao: QuestionAssignmentDao = db.questionAssignmentDao()
    private val instructorStudentAssignmentDao: InstructorStudentAssignmentDao = db.instructorStudentAssignmentDao()
    private val instructorProgramAssignmentDao: InstructorProgramAssignmentDao = db.instructorProgramAssignmentDao()
    private val evaluationResultDao: EvaluationResultDao = db.evaluationResultDao()
    private val scaleDao: ScaleDao = db.scaleDao()
    private val poiProgramAssignmentDao: PoiProgramAssignmentDao = db.poiProgramAssignmentDao()
    private val taskPoiAssignmentDao: TaskPoiAssignmentDao = db.taskPoiAssignmentDao()
    private val peclStudentDao: PeclStudentDao = db.peclStudentDao()

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
                    "Overview" to "Boat Operations,Team Leader Planning,ATL Planning,RTO Planning,Fire Support Marine Artillery,PFTCFT",
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
                    "Individual Data" to "PFTCFT",
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
                    ScaleEntity(scaleName = "Scale_Affect", options = "No Affect,Minor Affect,Neutral,Moderate Affect,Major Affect"),
                    ScaleEntity(scaleName = "Scale_Agree", options = "Strongly Agree,Agree,Neither Agree nor Disagree,Disagree,Strongly Disagree,NA"),
                    ScaleEntity(scaleName = "Scale_Aware", options = "Extremely Aware,Moderately Aware,Somewhat Aware,Slightly Aware,Not At All Aware"),
                    ScaleEntity(scaleName = "Scale_Comfort", options = "Very Comfortable,Comfortable,Neither Comfortable nor Uncomfortable,Uncomfortable,Very Uncomfortable"),
                    ScaleEntity(scaleName = "Scale_NASATLX", options = "1 - Very Low,2,3,4 – Medium,5,6,7 - Very High"),
                    ScaleEntity(scaleName = "Scale_Decrease", options = "Major Decrease,Moderate Decrease,Minor Decrease,No Effect"),
                    ScaleEntity(scaleName = "Scale_Difficulty", options = "Very Easy,Easy,Neutral,Difficult,Very Difficult"),
                    ScaleEntity(scaleName = "Scale_Ease_Of_Use", options = "Much Easier,Somewhat Easier,Not Easy or Difficult,Somewhat Difficult,Much More Difficult"),
                    ScaleEntity(scaleName = "Scale_Effective", options = "Extremely Effective,Effective,Neither Effective nor Ineffective,Ineffective,Extremely Ineffective"),
                    ScaleEntity(scaleName = "Scale_Improvement", options = "Major Improvement,Moderate Improvement,No Change,Moderate Decline,Major Decline"),
                    ScaleEntity(scaleName = "Scale_Frequency_5", options = "Always,Often,Sometimes,Rarely,Never"),
                    ScaleEntity(scaleName = "Scale_Frequency", options = "Never,Rarely (<10% of the time),Occasionally (~30% of the time),Sometimes (~50% of the time),Frequently (~70% of the time),Usually (~90% of the time),All the time (100%)"),
                    ScaleEntity(scaleName = "Scale_Importance", options = "Extremely important,Very important,Moderately important,Neutral,Slightly important,Low importance,Not at all important"),
                    ScaleEntity(scaleName = "Scale_Likelihood", options = "Almost Always True,Usually True,Occasionally True,Usually Not True,Almost Never True"),
                    ScaleEntity(scaleName = "Scale_Precipitation", options = "No rain,Drizzle,Light,Moderate,Heavy"),
                    ScaleEntity(scaleName = "Scale_Rating_Low_High", options = "LOW – 1,2,3,4,5,6,7,8,9,10 - High"),
                    ScaleEntity(scaleName = "Scale_Risk", options = "Catastrophic,Critical,Marginal,Negligible"),
                    ScaleEntity(scaleName = "Scale_Weather_Conditions", options = "Clear,Mostly Clear,Partly Cloudy,Mostly Cloudy,Cloudy"),
                    ScaleEntity(scaleName = "Scale_Yes_No", options = "YES,NO,N/A"),
                    ScaleEntity(scaleName = "Scale_Acceptability", options = "Perfectly Acceptable,Acceptable,Slightly acceptable,Neutral,Slightly unacceptable,Unacceptable,Totally unacceptable"),
                    ScaleEntity(scaleName = "Scale_Appropriateness", options = "Absolutely appropriate,Appropriate,Slightly appropriate,Neutral,Slightly inappropriate,Inappropriate,Absolutely inappropriate"),
                    ScaleEntity(scaleName = "Scale_Truth", options = "Always true,Usually true,Sometimes true,Neutral,Sometimes but infrequently true,Rarely true,Never true"),
                    ScaleEntity(scaleName = "Scale_Priority", options = "Essential priority,High priority,Neutral,Low priority,Not a priority"),
                    ScaleEntity(scaleName = "Scale_Concern", options = "Extremely concerned,Moderately concerned,Somewhat concerned,Slightly concerned,not at all concerned"),
                    ScaleEntity(scaleName = "Scale_Consideration", options = "Definitely consider,Might or might not consider,Would not consider"),
                    ScaleEntity(scaleName = "Scale_Support_Opposition", options = "Distinguished,Qualified,Unqualified"),
                    ScaleEntity(scaleName = "Scale_Probability", options = "Very probable,Somewhat probable,Neutral,Somewhat improbable,Not probable"),
                    ScaleEntity(scaleName = "Scale_Desirability", options = "Very desirable,Desirable,Neutral,Undesirable,Very undesirable"),
                    ScaleEntity(scaleName = "Scale_Use", options = "Every time,Almost every time,Occasionally/Sometimes,Almost never,Never use"),
                    ScaleEntity(scaleName = "Scale_Familiarity", options = "Extremely familiar,Moderately familiar,Somewhat familiar,Slightly familiar,not at all familiar"),
                    ScaleEntity(scaleName = "Scale_Barriers", options = "Not a barrier,Somewhat of a barrier,Moderate barrier,Extreme barrier"),
                    ScaleEntity(scaleName = "Scale_Satisfaction", options = "Very satisfied,Satisfied,Unsure,Dissatisfied,Very dissatisfied"),
                    ScaleEntity(scaleName = "Scale_Quality", options = "Excellent,Very good,Good,Fair,Poor"),
                    ScaleEntity(scaleName = "Scale_Comparison", options = "Much better,Somewhat better,About the same,Somewhat worse,Much worse"),
                    ScaleEntity(scaleName = "Scale_Responsibility", options = "Completely responsible,Mostly responsible,Somewhat responsible,Not at all responsible"),
                    ScaleEntity(scaleName = "Scale_Influence", options = "Extremely influential,Very influential,Somewhat influential,Slightly influential,Not at all influential"),
                    ScaleEntity(scaleName = "Scale_Increase", options = "Major Increase,Moderate Increase,Minor Increase,No Effect"),
                    ScaleEntity(scaleName = "Scale_Momentum", options = "Greatly Increased Momentum,Increased Momentum,No Change,Decreased Momentum"),
                    ScaleEntity(scaleName = "Scale_Adequate", options = "Extremely Adequate,Adequate,Indifferent,Inadequate"),
                    ScaleEntity(scaleName = "Scale_Assist", options = "Major Assist,Moderate Assist,Minor Assist,No Effect"),
                    ScaleEntity(scaleName = "Scale_Potential", options = "Major Potential,Moderate Potential,Minor Potential,No effect"),
                    ScaleEntity(scaleName = "Scale_Speed", options = "Extremely Rapid,Moderately Rapid,Fairly Rapid,Neither Rapid nor Slow"),
                    ScaleEntity(scaleName = "Scale_Challenge", options = "No Challenges,Mild Challenges,Moderate Challenges,Severe Challenges"),
                    ScaleEntity(scaleName = "Scale_Survivability", options = "Survivability 100% increase,Survivability 75-99% increase,Survivability 25-74% increase,Survivability 1-24% increase"),
                    ScaleEntity(scaleName = "Scale_Cognative", options = "Mild (no impact),Moderate (could still process data),Severe (difficultly processing data),Extreme (Could not process data)"),
                    ScaleEntity(scaleName = "Scale_Candidate", options = "Can 1,Can 2,Can 3,Can 4,Can 5,All Systems"),
                    ScaleEntity(scaleName = "Scale_Candidate2", options = "Current Candidate,Other Device"),
                    ScaleEntity(scaleName = "Scale_Cooper_Harper", options = "1,2,3,4,5,6,7,8,9,10"),
                    ScaleEntity(scaleName = "Scale_Fielding", options = "Field Immediately - No Improvements,Field Immediately - Minor Improvements,Field Soon - Moderate Improvements,No fielding until major improvements made"),
                    ScaleEntity(scaleName = "Scale_FieldingPercent", options = "No Improvements (100%),Fine Tune (~90% completion),Minor Improvements (~70% completion),Moderate Improvements (~50% completion),Major Improvements (~30% completion),Prototype Phase (<10% completion)"),
                    ScaleEntity(scaleName = "Scale_Swimmer_Qual", options = "WSB,WSI,WSA,MCIWS"),
                    ScaleEntity(scaleName = "Scale_Grade", options = "CIV,E1,E2,E3,E4,E5,E6,E7,E8,E9,O1,O2,O3,O4,O5,O6,O7,O8,O9,O10"),
                    ScaleEntity(scaleName = "Scale_StudentEval", options = "None,Very Little,Average,Above Average,Expert"),
                    ScaleEntity(scaleName = "Scale_PECL", options = "1,2,3,4,5"),
                    ScaleEntity(scaleName = "Scale_Go_NOGO", options = "GO,NO GO,NA"),
                    ScaleEntity(scaleName = "Scale_RSLC_Position", options = "Team Leader,Assistant Team Leader,RTO,Medic,Machine Gunner,Assistant Machine Gunner"),
                    ScaleEntity(scaleName = "Scale_Comment", options = ""),
                    ScaleEntity(scaleName = "Scale_Instructors", options = "Instructor1,Instructor2,Instructor3")
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

                // Questions (from QuestionData.kt)
                QuestionData.questionData.forEach { (question, taskName) ->
                    val taskId = taskMap[taskName]
                    if (taskId != null) {
                        val existing = peclQuestionDao.getQuestionBySubTask(question.subTask)
                        if (existing == null) {
                            val id = peclQuestionDao.insertQuestion(question)
                            questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = id, task_id = taskId))
                            Log.d("AppRepository", "Inserted question: ${question.subTask} with ID: $id assigned to task: $taskName")
                        } else {
                            Log.d("AppRepository", "Existing question: ${question.subTask} with ID: ${existing.id}")
                        }
                    } else {
                        Log.w("AppRepository", "Skipping question '${question.subTask}': No valid task found for '$taskName'")
                    }
                }

                // Prepopulate students
                val students = listOf(
                    PeclStudentEntity(id = 0, fullName = "John Doe", programId = programMap["AASB"]!!),
                    PeclStudentEntity(id = 0, fullName = "Jane Smith", programId = programMap["AASB"]!!)
                )
                students.forEach { peclStudentDao.insertStudent(it) }
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

    suspend fun getUserById(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)

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

    suspend fun updateTask(task: PeclTaskEntity, poiIds: List<Long>? = null): AppResult<Unit> {
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

    suspend fun getAssignmentForStudent(studentId: Long): InstructorStudentAssignmentEntity? = instructorStudentAssignmentDao.getAssignmentForStudent(studentId)

    suspend fun getInstructorName(instructorId: Long): String? = userDao.getInstructorName(instructorId)

    fun getProgramIdsForInstructor(instructorId: Long): Flow<List<Long>> = instructorProgramAssignmentDao.getProgramIdsForInstructor(instructorId)

    fun getAllQuestionsWithTasks(): Flow<List<QuestionWithTask>> = peclQuestionDao.getAllQuestionsWithTasks()

    fun getStudentsForInstructorAndProgram(instructorId: Long, programId: Long): Flow<List<PeclStudentEntity>> = instructorStudentAssignmentDao.getStudentsForInstructorAndProgram(instructorId, programId)

    suspend fun getTaskIdForQuestion(questionId: Long): Long? {
        return try {
            val assignment = questionAssignmentDao.getAssignmentByQuestionId(questionId).first()
            assignment?.task_id
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting task ID for question $questionId: ${e.message}", e)
            null
        }
    }
}