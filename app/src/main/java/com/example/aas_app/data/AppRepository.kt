package com.example.aas_app.data

import android.content.Context
import android.util.Log
import androidx.room.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val message: String) : AppResult<Nothing>()
}

class AppRepository(context: Context) {
    private val database: AppDatabase = AppDatabase.getDatabase(context)

    private val userDao = database.userDao()
    private val programDao = database.programDao()
    private val poiDao = database.poiDao()
    private val taskDao = database.taskDao()
    private val questionDao = database.questionDao()
    private val assignmentDao = database.assignmentDao()
    private val instructorStudentAssignmentDao = database.instructorStudentAssignmentDao()
    private val evaluationDao = database.evaluationDao()
    private val scaleDao = database.scaleDao()

    // User methods
    suspend fun getAllUsers(): AppResult<List<UserEntity>> = safeCall {
        userDao.getAllUsers()
    }

    suspend fun insertUser(user: UserEntity): AppResult<Unit> = safeCall {
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity): AppResult<Unit> = safeCall {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: UserEntity): AppResult<Unit> = safeDelete {
        userDao.deleteUser(user)
    }

    suspend fun getUsersByRole(role: String): AppResult<List<UserEntity>> = safeCall {
        userDao.getUsersByRole(role)
    }

    // Program methods
    suspend fun getAllPrograms(): AppResult<List<PeclProgramEntity>> = safeCall {
        programDao.getAllPrograms()
    }

    suspend fun insertProgram(program: PeclProgramEntity): AppResult<Long> = safeCall {
        programDao.insertProgram(program)
    }

    suspend fun updateProgram(program: PeclProgramEntity): AppResult<Unit> = safeCall {
        programDao.updateProgram(program)
    }

    suspend fun deleteProgram(program: PeclProgramEntity): AppResult<Unit> = safeDelete {
        programDao.deleteProgram(program)
    }

    suspend fun getProgramById(id: Long): PeclProgramEntity? {
        return programDao.getProgramById(id)
    }

    suspend fun getProgramByName(name: String): PeclProgramEntity? {
        return programDao.getProgramByName(name)
    }

    // POI methods
    suspend fun getPoisForProgram(programId: Long): AppResult<List<PeclPoiEntity>> = safeCall {
        poiDao.getPoisForProgram(programId)
    }

    suspend fun insertPoi(poi: PeclPoiEntity): AppResult<Long> = safeCallWithValidation(
        validation = {
            if (getProgramById(poi.programId) == null) "Parent program not found" else null
        }
    ) {
        poiDao.insertPoi(poi)
    }

    suspend fun updatePoi(poi: PeclPoiEntity): AppResult<Unit> = safeCall {
        poiDao.updatePoi(poi)
    }

    suspend fun deletePoi(poi: PeclPoiEntity): AppResult<Unit> = safeDelete {
        poiDao.deletePoi(poi)
    }

    suspend fun getPoiById(id: Long): PeclPoiEntity? {
        return poiDao.getPoiById(id)
    }

    suspend fun getPoiByName(name: String): PeclPoiEntity? {
        return poiDao.getPoiByName(name)
    }

    // Task methods
    suspend fun getTasksForPoi(poiId: Long): AppResult<List<PeclTaskEntity>> = safeCall {
        taskDao.getTasksForPoi(poiId)
    }

    suspend fun insertTask(task: PeclTaskEntity): AppResult<Long> = safeCallWithValidation(
        validation = {
            if (getPoiById(task.poiId) == null) "Parent POI not found" else null
        }
    ) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: PeclTaskEntity): AppResult<Unit> = safeCall {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: PeclTaskEntity): AppResult<Unit> = safeDelete {
        taskDao.deleteTask(task)
    }

    suspend fun getTaskById(id: Long): PeclTaskEntity? {
        return taskDao.getTaskById(id)
    }

    suspend fun getTaskByName(name: String): PeclTaskEntity? {
        return taskDao.getTaskByName(name)
    }

    // Question methods
    suspend fun insertQuestion(question: PeclQuestionEntity): AppResult<Long> = safeCall {
        questionDao.insertQuestion(question)
    }

    @Transaction
    suspend fun insertQuestionWithAssignment(question: PeclQuestionEntity, taskId: Long): AppResult<Long> = safeCallWithValidation(
        validation = {
            if (getTaskById(taskId) == null) "Task not found for assignment" else null
        }
    ) {
        val questionId = questionDao.insertQuestion(question)
        assignmentDao.insertAssignment(QuestionAssignmentEntity(questionId = questionId, taskId = taskId))
        questionId
    }

    suspend fun getQuestionsForTask(taskId: Long): AppResult<List<PeclQuestionEntity>> = safeCall {
        questionDao.getQuestionsForTask(taskId)
    }

    suspend fun updateQuestion(question: PeclQuestionEntity): AppResult<Unit> = safeCall {
        questionDao.updateQuestion(question)
    }

    suspend fun deleteQuestion(question: PeclQuestionEntity): AppResult<Unit> = safeDelete {
        questionDao.deleteQuestion(question)
    }

    suspend fun getQuestionById(id: Long): PeclQuestionEntity? {
        return questionDao.getQuestionById(id)
    }

    // Assignment methods (instructor-student)
    @Transaction
    suspend fun insertInstructorStudentAssignment(assignment: InstructorStudentAssignmentEntity): AppResult<Long> = safeCallWithValidation(
        validation = {
            val instructors = getUsersByRole("instructor")
            val students = getUsersByRole("student")
            if (instructors is AppResult.Error) "Failed to fetch instructors: ${instructors.message}" else if (students is AppResult.Error) "Failed to fetch students: ${students.message}" else if (!(instructors as AppResult.Success).data.any { it.id == assignment.instructorId }) "Instructor not found" else if (!(students as AppResult.Success).data.any { it.id == assignment.studentId }) "Student not found" else null
        }
    ) {
        instructorStudentAssignmentDao.insertAssignment(assignment)
    }

    suspend fun getStudentsForInstructor(instructorId: Long): AppResult<List<UserEntity>> = safeCall {
        instructorStudentAssignmentDao.getStudentsForInstructor(instructorId)
    }

    // Evaluation methods
    @Transaction
    suspend fun insertEvaluation(evaluation: PeclEvaluationResultEntity): AppResult<Long> = safeCallWithValidation(
        validation = {
            if (getQuestionById(evaluation.questionId) == null) "Question not found" else null
        }
    ) {
        evaluationDao.insertEvaluation(evaluation)
    }

    suspend fun getEvaluationsForStudent(studentId: Long, poiId: Long): AppResult<List<PeclEvaluationResultEntity>> = safeCall {
        evaluationDao.getEvaluationsForStudent(studentId, poiId)
    }

    // Scale methods
    suspend fun getAllScales(): AppResult<List<PeclScaleEntity>> = safeCall {
        scaleDao.getAllScales()
    }

    suspend fun insertScale(scale: PeclScaleEntity): AppResult<Unit> = safeCall {
        scaleDao.insertScale(scale)
    }

    suspend fun updateScale(scale: PeclScaleEntity): AppResult<Unit> = safeCall {
        scaleDao.updateScale(scale)
    }

    suspend fun deleteScale(scale: PeclScaleEntity): AppResult<Unit> = safeDelete {
        scaleDao.deleteScale(scale)
    }

    // Prepopulation (hierarchical, transactional)
    @Transaction
    suspend fun prePopulateAll() {
        withContext(Dispatchers.IO) {
            // database.clearAllTables() // Commented for safety; uncomment for dev reset

            val programNameToId = mutableMapOf<String, Long>()
            val poiNameToId = mutableMapOf<Pair<String, String>, Long>()
            val taskNameToId = mutableMapOf<Pair<String, String>, Long>()

            // Insert programs
            listOf("AASB", "RSLC").forEach { name ->
                getProgramByName(name) ?: run {
                    val id = programDao.insertProgram(PeclProgramEntity(name = name))
                    programNameToId[name] = id
                    Log.d("Prepop", "Inserted program $name with ID $id")
                }
            }

            // Insert POIs
            listOf(
                Triple("AASB", "Basic", ""),
                Triple("AASB", "Advanced", ""),
                Triple("RSLC", "Rappel", "")
                // Add more as per your overview
            ).forEach { (programName: String, poiName: String, desc: String) ->
                val programId = programNameToId[programName] ?: return@withContext
                val key = Pair(programName, poiName)
                if (getPoiByName(poiName) == null) {
                    val id = poiDao.insertPoi(PeclPoiEntity(name = poiName, programId = programId))
                    poiNameToId[key] = id
                    Log.d("Prepop", "Inserted POI $poiName for program $programName with ID $id")
                }
            }

            // Insert tasks
            listOf(
                Quad("AASB", "Basic", "Task1", "Description"),
                Quad("AASB", "Basic", "Task2", "Description")
                // Add more tasks per POI
            ).forEach { (programName: String, poiName: String, taskName: String, desc: String) ->
                val poiKey = Pair(programName, poiName)
                val poiId = poiNameToId[poiKey] ?: return@withContext
                val taskKey = Pair(poiName, taskName)
                if (getTaskByName(taskName) == null) {
                    val id = taskDao.insertTask(PeclTaskEntity(name = taskName, poiId = poiId))
                    taskNameToId[taskKey] = id
                    Log.d("Prepop", "Inserted task $taskName for POI $poiName with ID $id")
                }
            }

            // Insert questions and assignments
            listOf(
                Heptuple("AASB", "Basic", "Task1", "SubTask1", "TextBox", "Scale1", "Yes"),
                // Add more subtasks, e.g., Heptuple("AASB", "Basic", "Task1", "SubTask2", "ComboBox", "Scale2", "No")
            ).forEach { (programName: String, poiName: String, taskName: String, subTask: String, controlType: String, scale: String, critical: String) ->
                val taskKey = Pair(poiName, taskName)
                val taskId = taskNameToId[taskKey] ?: return@withContext
                val question = PeclQuestionEntity(subTask = subTask, controlType = controlType, scale = scale, criticalTask = critical)
                val questionId = questionDao.insertQuestion(question)
                assignmentDao.insertAssignment(QuestionAssignmentEntity(questionId = questionId, taskId = taskId))
                Log.d("Prepop", "Inserted question $subTask assigned to task $taskName")
            }

            // Insert example users and scales
            // e.g., insertUser(UserEntity(name = "Instructor1", role = "instructor"))
            // insertScale(PeclScaleEntity(scaleName = "Scale1", options = "Option1,Option2"))

            Log.d("Prepop", "Prepopulation completed successfully")
        }
    }

    // Helpers
    private suspend fun <T> safeCall(block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (e: Exception) {
            Log.e("AppRepository", "Error: ${e.message}", e)
            AppResult.Error(e.message ?: "Unknown error")
        }
    }

    private suspend fun <T> safeCallWithValidation(
        validation: suspend () -> String?,
        block: suspend () -> T
    ): AppResult<T> {
        return try {
            val errorMsg = validation()
            if (errorMsg != null) {
                return AppResult.Error(errorMsg)
            }
            AppResult.Success(block())
        } catch (e: Exception) {
            Log.e("AppRepository", "Validation/Error: ${e.message}", e)
            AppResult.Error(e.message ?: "Validation failed")
        }
    }

    private suspend fun safeDelete(block: suspend () -> Unit): AppResult<Unit> {
        return try {
            block()
            AppResult.Success(Unit)
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            Log.e("AppRepository", "Constraint violation: ${e.message}")
            AppResult.Error("Delete failed: Item is referenced elsewhere (e.g., has assigned children)")
        } catch (e: Exception) {
            Log.e("AppRepository", "Delete error: ${e.message}", e)
            AppResult.Error(e.message ?: "Delete failed")
        }
    }

    // Example report method
    suspend fun getAverageScoreForStudent(studentId: Long, poiId: Long): AppResult<Float> = safeCall {
        evaluationDao.getAverageScoreForStudent(studentId, poiId) ?: 0f
    }
}

// Tuple helpers
data class Quad<out A, out B, out C, out D>(val a: A, val b: B, val c: C, val d: D)

data class Heptuple<out A, out B, out C, out D, out E, out F, out G>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G
)