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
                for (name in ProgramData.programData) {
                    try {
                        val program = peclProgramDao.getProgramByName(name)
                        if (program == null) {
                            val id = peclProgramDao.insertProgram(PeclProgramEntity(name = name))
                            programMap[name] = id
                            Log.d("AppRepository", "Inserted program: $name with ID: $id")
                        } else {
                            programMap[name] = program.id
                            Log.d("AppRepository", "Existing program: $name with ID: ${program.id}")
                        }
                    } catch (e: Exception) {
                        Log.e("AppRepository", "Error processing program $name: ${e.message}", e)
                    }
                }

                // POIs
                val poiMap = mutableMapOf<String, Long>()
                for ((name, programNames) in PoiData.poiData) {
                    val programIds = programNames.mapNotNull { programMap[it] }
                    if (programIds.isNotEmpty()) {
                        try {
                            val poi = peclPoiDao.getPoiByName(name)
                            val poiId = if (poi == null) {
                                val id = peclPoiDao.insertPoi(PeclPoiEntity(name = name))
                                for (programId in programIds) {
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
                        } catch (e: Exception) {
                            Log.e("AppRepository", "Error processing POI $name: ${e.message}", e)
                        }
                    } else {
                        Log.w("AppRepository", "Skipping POI '$name': No valid programs found for '$programNames'")
                    }
                }

                // Tasks
                val taskMap = mutableMapOf<String, Long>()
                for ((name, poiNames) in TaskData.taskData) {
                    val poiIds = poiNames.mapNotNull { poiMap[it] }
                    if (poiIds.isNotEmpty()) {
                        try {
                            val task = peclTaskDao.getTaskByName(name)
                            val taskId = if (task == null) {
                                val id = peclTaskDao.insertTask(PeclTaskEntity(name = name))
                                for (poiId in poiIds) {
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
                        } catch (e: Exception) {
                            Log.e("AppRepository", "Error processing task $name: ${e.message}", e)
                        }
                    } else {
                        Log.w("AppRepository", "Skipping task '$name': No valid POIs found for '$poiNames'")
                    }
                }

                // Scales
                for (scale in ScaleData.scaleData) {
                    try {
                        val existing = scaleDao.getScaleByName(scale.scaleName)
                        if (existing == null) {
                            scaleDao.insertScale(scale)
                            Log.d("AppRepository", "Inserted scale: ${scale.scaleName}")
                        } else {
                            Log.d("AppRepository", "Existing scale: ${scale.scaleName}")
                        }
                    } catch (e: Exception) {
                        Log.e("AppRepository", "Error processing scale ${scale.scaleName}: ${e.message}", e)
                    }
                }

                // Questions
                for ((question, taskName) in QuestionData.questionData) {
                    val taskId = taskMap[taskName]
                    if (taskId != null) {
                        try {
                            val existing = peclQuestionDao.getQuestionBySubTask(question.subTask)
                            if (existing == null) {
                                val id = peclQuestionDao.insertQuestion(question)
                                questionAssignmentDao.insertAssignment(QuestionAssignmentEntity(question_id = id, task_id = taskId))
                                Log.d("AppRepository", "Inserted question: ${question.subTask} with ID: $id assigned to task: $taskName")
                            } else {
                                Log.d("AppRepository", "Existing question: ${question.subTask} with ID: ${existing.id}")
                            }
                        } catch (e: Exception) {
                            Log.e("AppRepository", "Error processing question ${question.subTask}: ${e.message}", e)
                        }
                    } else {
                        Log.w("AppRepository", "Skipping question '${question.subTask}': No valid task found for '$taskName'")
                    }
                }

                // Students
                for (student in PeclStudentData.studentData) {
                    try {
                        peclStudentDao.insertStudent(student)
                        Log.d("AppRepository", "Inserted student: ${student.fullName}")
                    } catch (e: Exception) {
                        Log.e("AppRepository", "Error processing student ${student.fullName}: ${e.message}", e)
                    }
                }

                // Instructors
                for ((fullName, programName) in PeclInstructorData.instructorData) {
                    try {
                        val existing = userDao.getUsersByRole("instructor").first().firstOrNull { it.fullName == fullName }
                        if (existing == null) {
                            val parts = fullName.split(" ")
                            val firstName = parts.dropLast(1).joinToString(" ")
                            val lastName = parts.last()
                            val user = UserEntity(firstName = firstName, lastName = lastName, grade = "", pin = null, fullName = fullName, role = "instructor")
                            val instructorId = userDao.insertUser(user)
                            val programId = programMap[programName]
                            if (programId != null) {
                                instructorProgramAssignmentDao.insertAssignment(InstructorProgramAssignmentEntity(instructor_id = instructorId, program_id = programId))
                                Log.d("AppRepository", "Assigned instructor: $fullName to program: $programName")
                            } else {
                                Log.w("AppRepository", "Skipping assignment for instructor '$fullName': Program '$programName' not found")
                            }
                            Log.d("AppRepository", "Inserted instructor: $fullName with ID: $instructorId")
                        } else {
                            Log.d("AppRepository", "Existing instructor: $fullName with ID: ${existing.id}")
                        }
                    } catch (e: Exception) {
                        Log.e("AppRepository", "Error processing instructor $fullName: ${e.message}", e)
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

    fun getUserById(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)

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

    fun getEvaluationResultsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>> {
        return evaluationResultDao.getEvaluationResultsForStudent(studentId)
    }

    fun getEvaluationsForStudentAndTask(studentId: Long, taskId: Long): Flow<List<PeclEvaluationResultEntity>> {
        return evaluationResultDao.getEvaluationsForStudentAndTask(studentId, taskId)
    }

    suspend fun deleteEvaluationResult(result: PeclEvaluationResultEntity): AppResult<Unit> {
        return try {
            evaluationResultDao.deleteEvaluationResultById(result.id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting evaluation result: ${e.message}", e)
        }
    }

    suspend fun deleteEvaluationsForStudentAndTask(studentId: Long, taskId: Long): AppResult<Unit> {
        return try {
            db.withTransaction {
                val evals = evaluationResultDao.getEvaluationsForStudentAndTask(studentId, taskId).first()
                evals.forEach { eval ->
                    evaluationResultDao.deleteEvaluationResultById(eval.id)
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error("Error deleting evaluations for task: ${e.message}", e)
        }
    }

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

    fun getAssignmentsForInstructor(instructorId: Long): Flow<List<InstructorStudentAssignmentEntity>> = instructorStudentAssignmentDao.getAssignmentsForInstructor(instructorId)

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

    // New: Insert evaluation result
    suspend fun insertEvaluationResult(result: PeclEvaluationResultEntity): AppResult<Long> {
        return try {
            val id = evaluationResultDao.insertEvaluationResult(result)
            AppResult.Success(id)
        } catch (e: Exception) {
            AppResult.Error("Error inserting evaluation result: ${e.message}", e)
        }
    }

    // New: Get students for program
    fun getStudentsForProgram(programId: Long): Flow<List<PeclStudentEntity>> {
        return instructorStudentAssignmentDao.getStudentsForProgram(programId)
    }
}