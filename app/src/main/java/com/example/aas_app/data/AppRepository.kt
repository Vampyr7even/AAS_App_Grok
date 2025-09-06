package com.example.aas_app.data

import android.util.Log
import androidx.room.withTransaction
import com.example.aas_app.data.dao.*
import com.example.aas_app.data.entity.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val exception: Exception) : AppResult<Nothing>()
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
    private val instructorProgramAssignmentDao: InstructorProgramAssignmentDao,
    private val poiProgramAssignmentDao: PoiProgramAssignmentDao,
    private val taskPoiAssignmentDao: TaskPoiAssignmentDao,
    private val demoTemplatesDao: DemoTemplatesDao,
    private val projectDao: ProjectDao,
    private val questionRepositoryDao: QuestionRepositoryDao,
    private val responseDao: ResponseDao
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

    suspend fun insertProgram(program: PeclProgramEntity): AppResult<Long> {
        return try {
            val id = peclProgramDao.insertProgram(program)
            AppResult.Success(id)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting program: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    fun getAllPrograms(): Flow<List<PeclProgramEntity>> = peclProgramDao.getAllPrograms()

    suspend fun updateProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            peclProgramDao.updateProgram(program)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating program: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun deleteProgram(program: PeclProgramEntity): AppResult<Unit> {
        return try {
            peclProgramDao.deleteProgram(program)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting program: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun insertPoi(poi: PeclPoiEntity, programIds: List<Long>): AppResult<Unit> {
        return try {
            val poiId = peclPoiDao.insertPoi(poi)
            programIds.forEach { programId ->
                poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = poiId, program_id = programId))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting POI: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    fun getAllPois(): Flow<List<PeclPoiEntity>> = peclPoiDao.getAllPois()

    fun getPoisForProgram(programId: Long): Flow<List<PeclPoiEntity>> = peclPoiDao.getPoisForProgram(programId)

    suspend fun updatePoi(poi: PeclPoiEntity, programIds: List<Long>): AppResult<Unit> {
        return try {
            peclPoiDao.updatePoi(poi)
            peclPoiDao.deletePoiProgramAssignmentsForPoi(poi.id)
            programIds.forEach { programId ->
                poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = poi.id, program_id = programId))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating POI: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun deletePoi(poi: PeclPoiEntity): AppResult<Unit> {
        return try {
            peclPoiDao.deletePoi(poi)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting POI: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun insertTask(task: PeclTaskEntity, poiIds: List<Long>): AppResult<Unit> {
        return try {
            val taskId = peclTaskDao.insertTask(task)
            poiIds.forEach { poiId ->
                taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = taskId, poi_id = poiId))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting task: ${e.message}", e)
            AppResult.Error(e)
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
                    taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = task.id, poi_id = poiId))
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating task: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun deleteTask(task: PeclTaskEntity): AppResult<Unit> {
        return try {
            peclTaskDao.deleteTask(task)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting task: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun insertQuestion(question: PeclQuestionEntity, taskId: Long): AppResult<Unit> {
        return try {
            val questionId = questionDao.insertQuestion(question)
            questionDao.insertQuestionAssignment(QuestionAssignmentEntity(question_id = questionId, task_id = taskId))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting question: ${e.message}", e)
            AppResult.Error(e)
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
            AppResult.Error(e)
        }
    }

    suspend fun deleteQuestion(question: PeclQuestionEntity): AppResult<Unit> {
        return try {
            questionDao.deleteQuestion(question)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting question: ${e.message}", e)
            AppResult.Error(e)
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
            AppResult.Error(e)
        }
    }

    suspend fun updateScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            scaleDao.updateScale(scale)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating scale: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun deleteScale(scale: ScaleEntity): AppResult<Unit> {
        return try {
            scaleDao.deleteScale(scale)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting scale: ${e.message}", e)
            AppResult.Error(e)
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
            AppResult.Error(e)
        }
    }

    suspend fun updatePeclStudent(student: PeclStudentEntity): AppResult<Unit> {
        return try {
            peclStudentDao.updateStudent(student)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating student: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun deletePeclStudent(student: PeclStudentEntity): AppResult<Unit> {
        return try {
            peclStudentDao.deleteStudent(student)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting student: ${e.message}", e)
            AppResult.Error(e)
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
            AppResult.Error(e)
        }
    }

    suspend fun insertComment(comment: CommentEntity): AppResult<Unit> {
        return try {
            commentDao.insertComment(comment)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting comment: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    fun getTaskGradeForStudent(studentId: Long, taskId: Long): Flow<Double?> = evaluationResultDao.getTaskGradeForStudent(studentId, taskId)

    suspend fun deleteEvaluationsForStudentAndTask(studentId: Long, taskId: Long): AppResult<Unit> {
        return try {
            evaluationResultDao.deleteEvaluationsForStudentAndTask(studentId, taskId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting evaluations: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    fun getAssignmentsForInstructor(instructorId: Long): Flow<List<InstructorStudentAssignmentEntity>> = instructorStudentAssignmentDao.getAssignmentsForInstructor(instructorId)

    fun getAssignmentForStudent(studentId: Long): Flow<InstructorStudentAssignmentEntity?> = flow {
        try {
            val assignment = instructorStudentAssignmentDao.getAssignmentForStudent(studentId)
            emit(assignment)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error getting assignment for student: ${e.message}", e)
            emit(null)
        }
    }

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

    suspend fun insertDemoTemplate(template: DemoTemplateEntity): AppResult<Unit> {
        return try {
            demoTemplatesDao.insert(template)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting demo template: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun getAllDemoTemplates(): List<DemoTemplateEntity> = demoTemplatesDao.getAllDemoTemplates()

    suspend fun updateDemoTemplate(template: DemoTemplateEntity): AppResult<Unit> {
        return try {
            demoTemplatesDao.update(template)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating demo template: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun deleteDemoTemplate(template: DemoTemplateEntity): AppResult<Unit> {
        return try {
            demoTemplatesDao.delete(template)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting demo template: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun getDemoTemplateById(id: Long): DemoTemplateEntity? = demoTemplatesDao.getTemplateById(id)

    suspend fun insertProject(project: ProjectEntity): AppResult<Unit> {
        return try {
            projectDao.insert(project)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting project: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun getAllProjects(): List<ProjectEntity> = projectDao.getAllProjects()

    suspend fun updateProject(project: ProjectEntity): AppResult<Unit> {
        return try {
            projectDao.update(project)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating project: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun deleteProject(project: ProjectEntity): AppResult<Unit> {
        return try {
            projectDao.delete(project)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting project: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun getProjectById(id: Long): ProjectEntity? = projectDao.getProjectById(id)

    suspend fun deleteAllProjects(): AppResult<Unit> {
        return try {
            projectDao.deleteAll()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting all projects: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun insertQuestionRepository(question: QuestionRepositoryEntity): AppResult<Unit> {
        return try {
            questionRepositoryDao.insert(question)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting question repository: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun updateQuestionRepository(question: QuestionRepositoryEntity): AppResult<Unit> {
        return try {
            questionRepositoryDao.update(question)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error updating question repository: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun deleteQuestionRepository(question: QuestionRepositoryEntity): AppResult<Unit> {
        return try {
            questionRepositoryDao.delete(question)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error deleting question repository: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun getAllQuestionsRepository(): List<QuestionRepositoryEntity> = questionRepositoryDao.getAllQuestions()

    suspend fun getQuestionsRepositoryByIds(ids: List<Long>): List<QuestionRepositoryEntity> = questionRepositoryDao.getQuestionsByIds(ids)

    suspend fun getQuestionRepositoryById(id: Long): QuestionRepositoryEntity? = questionRepositoryDao.getQuestionById(id)

    suspend fun insertResponse(response: ResponseEntity): AppResult<Unit> {
        return try {
            responseDao.insert(response)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("AppRepository", "Error inserting response: ${e.message}", e)
            AppResult.Error(e)
        }
    }

    suspend fun getResponsesForUser(userId: Long): List<ResponseEntity> = responseDao.getResponsesForUser(userId)

    suspend fun prePopulateAll() {
        appDatabase.withTransaction {
            try {
                // Insert Programs from ProgramData
                ProgramData.programData.forEach { name ->
                    peclProgramDao.insertProgram(PeclProgramEntity(name = name))
                }

                // Insert POIs and assignments from PoiData
                val programs = peclProgramDao.getAllPrograms().first()
                PoiData.poiData.forEach { (name, programNames) ->
                    val poiId = peclPoiDao.insertPoi(PeclPoiEntity(name = name))
                    programNames.forEach { programName ->
                        val programId = programs.firstOrNull { it.name == programName }?.id ?: 0L
                        if (programId != 0L) {
                            poiProgramAssignmentDao.insertAssignment(PoiProgramAssignmentEntity(poi_id = poiId, program_id = programId))
                        }
                    }
                }

                // Insert Tasks and assignments from TaskData
                val pois = peclPoiDao.getAllPois().first()
                TaskData.taskData.forEach { (name, poiNames) ->
                    val taskId = peclTaskDao.insertTask(PeclTaskEntity(name = name))
                    poiNames.forEach { poiName ->
                        val poiId = pois.firstOrNull { it.name == poiName }?.id ?: 0L
                        if (poiId != 0L) {
                            taskPoiAssignmentDao.insertAssignment(TaskPoiAssignmentEntity(task_id = taskId, poi_id = poiId))
                        }
                    }
                }

                // Insert Questions and assignments from QuestionData
                val tasks = peclTaskDao.getAllTasks().first()
                QuestionData.questionData.forEach { (questionEntity, taskName) ->
                    val questionId = questionDao.insertQuestion(questionEntity)
                    val taskId = tasks.firstOrNull { it.name == taskName }?.id ?: 0L
                    if (taskId != 0L) {
                        questionDao.insertQuestionAssignment(QuestionAssignmentEntity(question_id = questionId, task_id = taskId))
                    }
                }

                // Insert Scales from ScaleData
                ScaleData.scaleData.forEach { scale ->
                    scaleDao.insertScale(scale)
                }

                // Insert Instructors from PeclInstructorData as users with role 'instructor'
                val instructorPrograms = peclProgramDao.getAllPrograms().first()
                PeclInstructorData.instructorData.forEach { (fullName, programName) ->
                    val names = fullName.split(" ")
                    val firstName = names[0]
                    val lastName = names.getOrElse(1) { "" }
                    val userId = userDao.insertUser(UserEntity(firstName = firstName, lastName = lastName, fullName = fullName, grade = "", pin = null, role = "instructor"))
                    val programId = instructorPrograms.firstOrNull { it.name == programName }?.id ?: 0L
                    if (programId != 0L) {
                        instructorProgramAssignmentDao.insertAssignment(InstructorProgramAssignmentEntity(instructor_id = userId, program_id = programId))
                    }
                }

                // Insert Students from PeclStudentData
                PeclStudentData.studentData.forEach { student ->
                    peclStudentDao.insertStudent(student)
                }
            } catch (e: Exception) {
                Log.e("AppRepository", "Error pre-populating database: ${e.message}", e)
                throw e
            }
        }
    }
}