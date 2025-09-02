package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.AppResult
import com.example.aas_app.data.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.sortedBy

@HiltViewModel
class PeclViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _tasksState = MutableLiveData<AppState<List<PeclTaskEntity>>>()
    val tasksState: LiveData<AppState<List<PeclTaskEntity>>> = _tasksState

    private val _questionsState = MutableLiveData<AppState<List<PeclQuestionEntity>>>()
    val questionsState: LiveData<AppState<List<PeclQuestionEntity>>> = _questionsState

    private val _evaluationResultsState = MutableLiveData<AppState<List<PeclEvaluationResultEntity>>>()
    val evaluationResultsState: LiveData<AppState<List<PeclEvaluationResultEntity>>> = _evaluationResultsState

    private val _commentsState = MutableLiveData<AppState<List<String>>>()
    val commentsState: LiveData<AppState<List<String>>> = _commentsState

    private val _studentsState = MutableLiveData<AppState<List<PeclStudentEntity>>>()
    val studentsState: LiveData<AppState<List<PeclStudentEntity>>> = _studentsState

    private val _poisState = MutableLiveData<AppState<List<PeclPoiEntity>>>()
    val poisState: LiveData<AppState<List<PeclPoiEntity>>> = _poisState

    private val _instructorsState = MutableLiveData<AppState<List<UserEntity>>>()
    val instructorsState: LiveData<AppState<List<UserEntity>>> = _instructorsState

    private val _programsForInstructorState = MutableLiveData<AppState<List<PeclProgramEntity>>>()
    val programsForInstructorState: LiveData<AppState<List<PeclProgramEntity>>> = _programsForInstructorState

    private val _studentsForInstructorAndProgramState = MutableLiveData<AppState<List<PeclStudentEntity>>>()
    val studentsForInstructorAndProgramState: LiveData<AppState<List<PeclStudentEntity>>> = _studentsForInstructorAndProgramState

    private val _evaluationsForStudentAndTaskState = MutableLiveData<AppState<List<PeclEvaluationResultEntity>>>()
    val evaluationsForStudentAndTaskState: LiveData<AppState<List<PeclEvaluationResultEntity>>> = _evaluationsForStudentAndTaskState

    fun loadTasksForPoi(poiId: Long) {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getTasksForPoi(poiId).first()
                _tasksState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading tasks: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error loading tasks"))
            }
        }
    }

    fun loadAllTasks() {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllTasks().first()
                _tasksState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading all tasks: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error loading tasks"))
            }
        }
    }

    fun loadQuestionsForTask(taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForTask(taskId).first()
                _questionsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun loadQuestionsForPoi(poiId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForPoi(poiId).first()
                _questionsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions for POI"))
            }
        }
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            try {
                val id = repository.insertEvaluationResult(result)
                when (id) {
                    is AppResult.Success -> _evaluationResultsState.postValue(AppState.Success<List<PeclEvaluationResultEntity>>(listOf(result)))
                    is AppResult.Error -> _evaluationResultsState.postValue(AppState.Error(id.message))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting evaluation: ${e.message}", e)
                _evaluationResultsState.postValue(AppState.Error(e.message ?: "Error inserting evaluation"))
            }
        }
    }

    fun loadEvaluationResultsForStudent(studentId: Long) {
        _evaluationResultsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getEvaluationResultsForStudent(studentId).first()
                _evaluationResultsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading evaluation results: ${e.message}", e)
                _evaluationResultsState.postValue(AppState.Error(e.message ?: "Error loading evaluation results"))
            }
        }
    }

    fun loadCommentsForStudent(studentId: Long) {
        _commentsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getEvaluationResultsForStudent(studentId).first().map { it.comment }.filter { it.isNotBlank() }
                _commentsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading comments: ${e.message}", e)
                _commentsState.postValue(AppState.Error(e.message ?: "Error loading comments"))
            }
        }
    }

    fun loadStudentsForProgram(programId: Long) {
        _studentsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForProgram(programId).first()
                _studentsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading students for program: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error loading students"))
            }
        }
    }

    fun loadPois() {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPois().first()
                _poisState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading POIs: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error loading POIs"))
            }
        }
    }

    fun loadStudents() {
        _studentsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPeclStudents().first()
                _studentsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading students: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error loading students"))
            }
        }
    }

    fun insertPeclStudent(student: PeclStudentEntity) {
        _studentsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.insertPeclStudent(student)
            when (result) {
                is AppResult.Success -> {
                    loadStudents()
                    _studentsState.postValue(AppState.Success(repository.getAllPeclStudents().first()))
                }
                is AppResult.Error -> _studentsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun updatePeclStudent(student: PeclStudentEntity) {
        _studentsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.updatePeclStudent(student)
            when (result) {
                is AppResult.Success -> {
                    loadStudents()
                    _studentsState.postValue(AppState.Success(repository.getAllPeclStudents().first()))
                }
                is AppResult.Error -> _studentsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun deletePeclStudent(student: PeclStudentEntity) {
        _studentsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.deletePeclStudent(student)
            when (result) {
                is AppResult.Success -> {
                    loadStudents()
                    _studentsState.postValue(AppState.Success(repository.getAllPeclStudents().first()))
                }
                is AppResult.Error -> _studentsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun loadInstructors() {
        _instructorsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getUsersByRole("instructor").first()
                _instructorsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading instructors: ${e.message}", e)
                _instructorsState.postValue(AppState.Error(e.message ?: "Error loading instructors"))
            }
        }
    }

    fun loadProgramsForInstructor(instructorId: Long) {
        _programsForInstructorState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val programIds = repository.getProgramIdsForInstructor(instructorId).first()
                val programs = programIds.mapNotNull { repository.getProgramById(it) }
                _programsForInstructorState.postValue(AppState.Success(programs))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading programs for instructor: ${e.message}", e)
                _programsForInstructorState.postValue(AppState.Error(e.message ?: "Error loading programs"))
            }
        }
    }

    fun loadPoisForProgram(programId: Long) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getPoisForProgram(programId).first()
                _poisState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading POIs for program: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error loading POIs"))
            }
        }
    }

    fun loadStudentsForInstructorAndProgram(instructorId: Long, programId: Long) {
        _studentsForInstructorAndProgramState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForInstructorAndProgram(instructorId, programId).first()
                _studentsForInstructorAndProgramState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading students for instructor and program: ${e.message}", e)
                _studentsForInstructorAndProgramState.postValue(AppState.Error(e.message ?: "Error loading students"))
            }
        }
    }

    fun loadAllQuestions() {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllQuestions().map { questions ->
                    questions.sortedBy { it.subTask }
                }.first()
                _questionsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading all questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun insertQuestion(question: PeclQuestionEntity, taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.insertQuestion(question, taskId)
            when (result) {
                is AppResult.Success -> loadAllQuestions()
                is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun updateQuestion(question: PeclQuestionEntity, taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.updateQuestion(question, taskId)
            when (result) {
                is AppResult.Success -> loadAllQuestions()
                is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun deleteQuestion(question: PeclQuestionEntity) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.deleteQuestion(question)
            when (result) {
                is AppResult.Success -> loadAllQuestions()
                is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun getTaskForQuestion(questionId: Long, onResult: (PeclTaskEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val taskId = repository.getTaskIdForQuestion(questionId)
                val task = taskId?.let { repository.getTaskById(it) }
                onResult(task)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting task for question $questionId: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getInstructorById(instructorId: Long, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val instructor = repository.getUserById(instructorId).first()
                onResult(instructor)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting instructor $instructorId: ${e.message}", e)
                onResult(null)
            }
        }
    }

    suspend fun getAssignmentForStudent(studentId: Long): InstructorStudentAssignmentEntity? {
        return try {
            repository.getAssignmentForStudent(studentId)
        } catch (e: Exception) {
            Log.e("PeclViewModel", "Error getting assignment for student $studentId: ${e.message}", e)
            null
        }
    }

    suspend fun getProgramById(programId: Long): PeclProgramEntity? {
        return try {
            repository.getProgramById(programId)
        } catch (e: Exception) {
            Log.e("PeclViewModel", "Error getting program $programId: ${e.message}", e)
            null
        }
    }

    fun getPeclStudentById(studentId: Long, onResult: (PeclStudentEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val student = repository.getPeclStudentById(studentId).first()
                onResult(student)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting student $studentId: ${e.message}", e)
                onResult(null)
            }
        }
    }

    suspend fun getScaleByName(scaleName: String): ScaleEntity? {
        return try {
            repository.getScaleByName(scaleName)
        } catch (e: Exception) {
            Log.e("PeclViewModel", "Error getting scale $scaleName: ${e.message}", e)
            null
        }
    }

    fun getTaskById(taskId: Long, onResult: (PeclTaskEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val task = repository.getTaskById(taskId)
                onResult(task)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting task $taskId: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getEvaluationsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>> {
        return repository.getEvaluationResultsForStudent(studentId)
    }

    fun loadTaskGradeForStudent(studentId: Long, taskId: Long): Flow<Double?> {
        return repository.getEvaluationResultsForStudent(studentId)
            .map { evaluations ->
                evaluations.firstOrNull { it.task_id == taskId }?.task_grade
            }
    }

    fun loadEvaluationsForStudentAndTask(studentId: Long, taskId: Long) {
        _evaluationsForStudentAndTaskState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getEvaluationsForStudentAndTask(studentId, taskId).first()
                _evaluationsForStudentAndTaskState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading evaluations for student and task: ${e.message}", e)
                _evaluationsForStudentAndTaskState.postValue(AppState.Error(e.message ?: "Error loading evaluations"))
            }
        }
    }

    fun deleteEvaluationsForStudentAndTask(studentId: Long, taskId: Long) {
        viewModelScope.launch {
            try {
                val result = repository.deleteEvaluationsForStudentAndTask(studentId, taskId)
                when (result) {
                    is AppResult.Success -> _evaluationsForStudentAndTaskState.postValue(AppState.Success(emptyList()))
                    is AppResult.Error -> _evaluationsForStudentAndTaskState.postValue(AppState.Error(result.message))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error deleting evaluations for task: ${e.message}", e)
                _evaluationsForStudentAndTaskState.postValue(AppState.Error(e.message ?: "Error deleting evaluations"))
            }
        }
    }
}