package com.example.aas_app.data

import android.util.Log
import androidx.room.withTransaction
import com.example.aas_app.data.entity.*
import com.example.aas_app.data.dao.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String) : AppResult<Nothing>()
}

@Singleton
class AppRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val userDao: UserDao,
    private val peclProgramDao: PeclProgramDao,
    private val peclPoiDao: PeclPoiDao,
    private val peclTaskDao: PeclTaskDao,
    private val questionDao: QuestionDao,
    private val scaleDao: ScaleDao,
    private val peclStudentDao: PeclStudentDao,
    private val commentDao: CommentDao,
    private val evaluationResultDao: EvaluationResultDao,
    private val instructorStudentAssignmentDao: InstructorStudentAssignmentDao,
    private val instructorProgramAssignmentDao: InstructorProgramAssignmentDao
) {

    suspend fun insertUser(user: UserEntity): Long {
        return try {
            userDao.insertUser(user)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting user: ${e.message}", e)
            throw e
        }
    }

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getUsersByRole(role: String): Flow<List<UserEntity>> = userDao.getUsersByRole(role)

    fun getUserById(id: Long): Flow<UserEntity?> = userDao.getUserById(id)

    suspend fun updateUser(user: UserEntity) {
        try {
            userDao.updateUser(user)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating user: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteUser(user: UserEntity) {
        try {
            userDao.deleteUser(user)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting user: ${e.message}", e)
            throw e
        }
    }

    suspend fun insertProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            peclProgramDao.insertProgram(program)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting program: ${e.message}", e)
            AppResult.Error(e.message ?: "Error inserting program")
        }
    }

    fun getAllPrograms(): Flow<List<PeclProgramEntity>> = peclProgramDao.getAllPrograms()

    suspend fun updateProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            peclProgramDao.updateProgram(program)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating program: ${e.message}", e)
            AppResult.Error(e.message ?: "Error updating program")
        }
    }

    suspend fun deleteProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            peclProgramDao.deleteProgram(program)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting program: ${e.message}", e)
            AppResult.Error(e.message ?: "Error deleting program")
        }
    }

    suspend fun insertPoi(poi: PeclPoiEntity, programIds: List<Long>): AppResult<Unit> {
        return try {
            val poiId = peclPoiDao.insertPoi(poi)
            programIds.forEach { programId ->
                peclPoiDao.insertPoiProgramAssignment(PoiProgramAssignmentEntity(poi_id = poiId, program_id = programId))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting POI: ${e.message}", e)
            AppResult.Error(e.message ?: "Error inserting POI")
        }
    }

    fun getAllPois(): Flow<List<PeclPoiEntity>> = peclPoiDao.getAllPois()

    fun getPoisForProgram(programId: Long): Flow<List<PeclPoiEntity>> = peclPoiDao.getPoisForProgram(programId)

    suspend fun updatePoi(poi: PeclPoiEntity, programIds: List<Long>): AppResult<Unit> {
        return try {
            peclPoiDao.updatePoi(poi)
            peclPoiDao.deletePoiProgramAssignmentsForPoi(poi.id)
            programIds.forEach { programId ->
                peclPoiDao.insertPoiProgramAssignment(PoiProgramAssignmentEntity(poi_id = poi.id, program_id = programId))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating POI: ${e.message}", e)
            AppResult.Error(e.message ?: "Error updating POI")
        }
    }

    suspend fun deletePoi(poi: PeclPoiEntity): AppResult<Unit> {
        return try {
            peclPoiDao.deletePoi(poi)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting POI: ${e.message}", e)
            AppResult.Error(e.message ?: "Error deleting POI")
        }
    }

    suspend fun insertTask(task: PeclTaskEntity, poiIds: List<Long>): AppResult<Unit> {
        return try {
            val taskId = peclTaskDao.insertTask(task)
            poiIds.forEach { poiId ->
                peclTaskDao.insertTaskPoiAssignment(TaskPoiAssignmentEntity(task_id = taskId, poi_id = poiId))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting task: ${e.message}", e)
            AppResult.Error(e.message ?: "Error inserting task")
        }
    }

    fun getAllTasks(): Flow<List<PeclTaskEntity>> = peclTaskDao.getAllTasks()

    fun getTasksForPoi(poiId: Long): Flow<List<PeclTaskEntity>> = peclTaskDao.getTasksForPoi(poiId)

    suspend fun updateTask(task: PeclTaskEntity, poiIds: List<Long>?): AppResult<Unit> {
        return try {
            peclTaskDao.updateTask(task)
            if (poiIds != null) {
                peclTaskDao.deleteTaskPoiAssignmentsForTask(task.id)
                poiIds.forEach { poiId ->
                    peclTaskDao.insertTaskPoiAssignment(TaskPoiAssignmentEntity(task_id = task.id, poi_id = poiId))
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating task: ${e.message}", e)
            AppResult.Error(e.message ?: "Error updating task")
        }
    }

    suspend fun deleteTask(task: PeclTaskEntity): AppResult<Unit> {
        return try {
            peclTaskDao.deleteTask(task)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting task: ${e.message}", e)
            AppResult.Error(e.message ?: "Error deleting task")
        }
    }

    suspend fun insertQuestion(question: PeclQuestionEntity, taskId: Long): AppResult<Unit> {
        return try {
            val questionId = questionDao.insertQuestion(question)
            questionDao.insertQuestionAssignment(QuestionAssignmentEntity(question_id = questionId, task_id = taskId))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting question: ${e.message}", e)
            AppResult.Error(e.message ?: "Error inserting question")
        }
    }

    fun getAllQuestions(): Flow<List<PeclQuestionEntity>> = questionDao.getAllQuestions()

    fun getQuestionsForTask(taskId: Long): Flow<List<PeclQuestionEntity>> = questionDao.getQuestionsForTask(taskId)

    fun getQuestionsForPoi(poiId: Long): Flow<List<PeclQuestionEntity>> = questionDao.getQuestionsForPoi(poiId)

    suspend fun updateQuestion(question: PeclQuestionEntity, taskId: Long): AppResult<Unit> {
        return try {
            questionDao.updateQuestion(question)
            questionDao.deleteQuestionTaskAssignmentsForQuestion(question.id)
            questionDao.insertQuestionAssignment(QuestionAssignmentEntity(question_id = question.id, task_id = taskId))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating question: ${e.message}", e)
            AppResult.Error(e.message ?: "Error updating question")
        }
    }

    suspend fun deleteQuestion(question: PeclQuestionEntity): AppResult<Unit> {
        return try {
            questionDao.deleteQuestion(question)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting question: ${e.message}", e)
            AppResult.Error(e.message ?: "Error deleting question")
        }
    }

    suspend fun getQuestionById(id: Long): PeclQuestionEntity? = questionDao.getQuestionById(id)

    suspend fun getTaskIdForQuestion(questionId: Long): Long? = questionDao.getTaskIdForQuestion(questionId)

    fun getAllQuestionsWithTasks(): Flow<List<QuestionWithTask>> = questionDao.getAllQuestionsWithTasks()

    fun getAllScales(): Flow<List<ScaleEntity>> = scaleDao.getAllScales()

    suspend fun insertScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            scaleDao.insertScale(scale)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting scale: ${e.message}", e)
            AppResult.Error(e.message ?: "Error inserting scale")
        }
    }

    suspend fun updateScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            scaleDao.updateScale(scale)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating scale: ${e.message}", e)
            AppResult.Error(e.message ?: "Error updating scale")
        }
    }

    suspend fun deleteScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            scaleDao.deleteScale(scale)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting scale: ${e.message}", e)
            AppResult.Error(e.message ?: "Error deleting scale")
        }
    }

    suspend fun getScaleById(id: Long): ScaleEntity? = scaleDao.getScaleById(id)

    suspend fun getScaleByName(name: String): ScaleEntity? = scaleDao.getScaleByName(name)

    suspend fun getTaskById(id: Long): PeclTaskEntity? = peclTaskDao.getTaskById(id)

    fun getAllPeclStudents(): Flow<List<PeclStudentEntity>> = peclStudentDao.getAllStudents()

    suspend fun insertPeclStudent(student: PeclStudentEntity): AppResult<Unit> {
        return try {
            peclStudentDao.insertStudent(student)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting student: ${e.message}", e)
            AppResult.Error(e.message ?: "Error inserting student")
        }
    }

    suspend fun updatePeclStudent(student: PeclStudentEntity): AppResult<Unit> {
        return try {
            peclStudentDao.updateStudent(student)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating student: ${e.message}", e)
            AppResult.Error(e.message ?: "Error updating student")
        }
    }

    suspend fun deletePeclStudent(student: PeclStudentEntity): AppResult<Unit> {
        return try {
            peclStudentDao.deleteStudent(student)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting student: ${e.message}", e)
            AppResult.Error(e.message ?: "Error deleting student")
        }
    }

    fun getPeclStudentById(studentId: Long): Flow<PeclStudentEntity?> = peclStudentDao.getStudentById(studentId)

    fun getStudentsForInstructorAndProgram(instructorId: Long, programId: Long): Flow<List<PeclStudentEntity>> = peclStudentDao.getStudentsForInstructorAndProgram(instructorId, programId)

    fun getCommentsForStudent(studentId: Long): Flow<List<CommentEntity>> = commentDao.getCommentsForStudent(studentId)

    fun getEvaluationResultsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>> = evaluationResultDao.getEvaluationResultsForStudent(studentId)

    fun getEvaluationsForStudentAndTask(studentId: Long, taskId: Long): Flow<List<PeclEvaluationResultEntity>> = evaluationResultDao.getEvaluationsForStudentAndTask(studentId, taskId)

    suspend fun insertEvaluationResult(result: PeclEvaluationResultEntity): AppResult<Unit> {
        return try {
            evaluationResultDao.insert(result)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting evaluation result: ${e.message}", e)
            AppResult.Error(e.message ?: "Error inserting evaluation result")
        }
    }

    suspend fun insertComment(comment: CommentEntity): AppResult<Unit> {
        return try {
            commentDao.insertComment(comment)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting comment: ${e.message}", e)
            AppResult.Error(e.message ?: "Error inserting comment")
        }
    }

    fun getTaskGradeForStudent(studentId: Long, taskId: Long): Flow<Double?> = evaluationResultDao.getTaskGradeForStudent(studentId, taskId)

    suspend fun deleteEvaluationsForStudentAndTask(studentId: Long, taskId: Long): AppResult<Unit> {
        return try {
            evaluationResultDao.deleteEvaluationsForStudentAndTask(studentId, taskId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting evaluations: ${e.message}", e)
            AppResult.Error(e.message ?: "Error deleting evaluations")
        }
    }

    fun getAssignmentsForInstructor(instructorId: Long): Flow<List<InstructorStudentAssignmentEntity>> = instructorStudentAssignmentDao.getAssignmentsForInstructor(instructorId)

    fun getAssignmentsForStudent(studentId: Long): Flow<List<InstructorStudentAssignmentEntity>> = instructorStudentAssignmentDao.getAssignmentsForStudent(studentId)

    suspend fun insertAssignment(assignment: InstructorStudentAssignmentEntity) {
        try {
            instructorStudentAssignmentDao.insertAssignment(assignment)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting assignment: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteAssignmentsForInstructor(instructorId: Long) {
        try {
            instructorStudentAssignmentDao.deleteAssignmentsForInstructor(instructorId)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting assignments: ${e.message}", e)
            throw e
        }
    }

    suspend fun insertInstructorProgramAssignment(assignment: InstructorProgramAssignmentEntity) {
        try {
            instructorProgramAssignmentDao.insertAssignment(assignment)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting instructor program assignment: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteInstructorProgramAssignmentsForInstructor(instructorId: Long) {
        try {
            instructorProgramAssignmentDao.deleteAssignmentsForInstructor(instructorId)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting instructor program assignments: ${e.message}", e)
            throw e
        }
    }

    fun getProgramsForInstructor(instructorId: Long): Flow<List<PeclProgramEntity>> = instructorProgramAssignmentDao.getProgramsForInstructor(instructorId)

    fun getProgramIdsForInstructor(instructorId: Long): Flow<List<Long>> = instructorProgramAssignmentDao.getProgramIdsForInstructor(instructorId)

    suspend fun getProgramById(programId: Long): PeclProgramEntity? = peclProgramDao.getProgramById(programId)

    fun getProgramsForPoi(poiId: Long): Flow<List<PeclProgramEntity>> = peclPoiDao.getProgramsForPoi(poiId)

    fun getPoisForTask(taskId: Long): Flow<List<PeclPoiEntity>> = peclTaskDao.getPoisForTask(taskId)

    suspend fun getInstructorName(instructorId: Long): String? {
        return try {
            userDao.getInstructorName(instructorId)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting instructor name: ${e.message}", e)
            null
        }
    }

    fun prePopulateAll() {
        runBlocking {
            appDatabase.withTransaction {
                try {
                    // Example data for programs
                    peclProgramDao.insertProgram(PeclProgramEntity(id = 0, name = "Program A"))
                    peclProgramDao.insertProgram(PeclProgramEntity(id = 0, name = "Program B"))

                    // Example data for POIs
                    val poiId1 = peclPoiDao.insertPoi(PeclPoiEntity(id = 0, name = "POI 1"))
                    val poiId2 = peclPoiDao.insertPoi(PeclPoiEntity(id = 0, name = "POI 2"))
                    peclPoiDao.insertPoiProgramAssignment(PoiProgramAssignmentEntity(poi_id = poiId1, program_id = 1))
                    peclPoiDao.insertPoiProgramAssignment(PoiProgramAssignmentEntity(poi_id = poiId2, program_id = 2))

                    // Example data for tasks
                    val taskId1 = peclTaskDao.insertTask(PeclTaskEntity(id = 0, name = "Task 1"))
                    val taskId2 = peclTaskDao.insertTask(PeclTaskEntity(id = 0, name = "Task 2"))
                    peclTaskDao.insertTaskPoiAssignment(TaskPoiAssignmentEntity(task_id = taskId1, poi_id = poiId1))
                    peclTaskDao.insertTaskPoiAssignment(TaskPoiAssignmentEntity(task_id = taskId2, poi_id = poiId2))

                    // Example data for questions
                    val questionId1 = questionDao.insertQuestion(PeclQuestionEntity(id = 0, subTask = "SubTask 1", controlType = "ComboBox", scale = "Scale_PECL", criticalTask = "YES"))
                    val questionId2 = questionDao.insertQuestion(PeclQuestionEntity(id = 0, subTask = "SubTask 2", controlType = "TextBox", scale = "Scale_Yes_No", criticalTask = "NO"))
                    questionDao.insertQuestionAssignment(QuestionAssignmentEntity(question_id = questionId1, task_id = taskId1))
                    questionDao.insertQuestionAssignment(QuestionAssignmentEntity(question_id = questionId2, task_id = taskId2))

                    // Example data for students
                    peclStudentDao.insertStudent(PeclStudentEntity(id = 0, firstName = "John", lastName = "Doe", fullName = "Doe, John", grade = "A", pin = 1234))
                    peclStudentDao.insertStudent(PeclStudentEntity(id = 0, firstName = "Jane", lastName = "Smith", fullName = "Smith, Jane", grade = "B", pin = 5678))

                    // Example data for instructors
                    val instructorId = userDao.insertUser(UserEntity(id = 0, firstName = "Instructor", lastName = "One", fullName = "One, Instructor", grade = "", pin = null, role = "instructor"))
                    instructorStudentAssignmentDao.insertAssignment(InstructorStudentAssignmentEntity(instructor_id = instructorId, student_id = 1, program_id = 1))
                } catch (e: Exception) {
                    Log.e("AppRepository", "Error pre-populating database: ${e.message}", e)
                    throw e
                }
            }
        }
    }
}