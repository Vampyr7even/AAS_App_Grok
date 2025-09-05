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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeclViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _instructorsState = MutableLiveData<AppState<List<UserEntity>>>()
    val instructorsState: LiveData<AppState<List<UserEntity>>> = _instructorsState

    private val _programsForInstructorState = MutableLiveData<AppState<List<PeclProgramEntity>>>()
    val programsForInstructorState: LiveData<AppState<List<PeclProgramEntity>>> = _programsForInstructorState

    private val _poisState = MutableLiveData<AppState<List<PeclPoiEntity>>>()
    val poisState: LiveData<AppState<List<PeclPoiEntity>>> = _poisState

    private val _studentsState = MutableLiveData<AppState<List<PeclStudentEntity>>>()
    val studentsState: LiveData<AppState<List<PeclStudentEntity>>> = _studentsState

    private val _studentsForInstructorAndProgramState = MutableLiveData<AppState<List<PeclStudentEntity>>>()
    val studentsForInstructorAndProgramState: LiveData<AppState<List<PeclStudentEntity>>> = _studentsForInstructorAndProgramState

    private val _tasksState = MutableLiveData<AppState<List<PeclTaskEntity>>>()
    val tasksState: LiveData<AppState<List<PeclTaskEntity>>> = _tasksState

    private val _evaluationResultsState = MutableLiveData<AppState<List<PeclEvaluationResultEntity>>>()
    val evaluationResultsState: LiveData<AppState<List<PeclEvaluationResultEntity>>> = _evaluationResultsState

    private val _commentsState = MutableLiveData<AppState<List<CommentEntity>>>()
    val commentsState: LiveData<AppState<List<CommentEntity>>> = _commentsState

    private val _evaluationsForStudentAndTaskState = MutableLiveData<AppState<List<PeclEvaluationResultEntity>>>()
    val evaluationsForStudentAndTaskState: LiveData<AppState<List<PeclEvaluationResultEntity>>> = _evaluationsForStudentAndTaskState

    private val _questionsState = MutableLiveData<AppState<List<PeclQuestionEntity>>>()
    val questionsState: LiveData<AppState<List<PeclQuestionEntity>>> = _questionsState

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
                val data = repository.getProgramsForInstructor(instructorId).first()
                _programsForInstructorState.postValue(AppState.Success(data))
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

    fun loadTasksForPoi(poiId: Long) {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getTasksForPoi(poiId).first()
                _tasksState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading tasks for POI: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error loading tasks"))
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

    fun loadStudentsForInstructor(instructorId: Long) {
        _studentsForInstructorAndProgramState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForInstructorAndProgram(instructorId, 0L).first()
                _studentsForInstructorAndProgramState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading students for instructor: ${e.message}", e)
                _studentsForInstructorAndProgramState.postValue(AppState.Error(e.message ?: "Error loading students"))
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
                val data = repository.getCommentsForStudent(studentId).first()
                _commentsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading comments: ${e.message}", e)
                _commentsState.postValue(AppState.Error(e.message ?: "Error loading comments"))
            }
        }
    }

    fun getEvaluationsForStudent(studentId: Long) = repository.getEvaluationResultsForStudent(studentId)

    fun getProgramById(programId: Long): PeclProgramEntity? {
        var program: PeclProgramEntity? = null
        viewModelScope.launch {
            try {
                program = repository.getProgramById(programId)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting program by ID: ${e.message}", e)
            }
        }
        return program
    }

    fun getInstructorById(instructorId: Long, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val instructor = repository.getUserById(instructorId).first()
                onResult(instructor)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting instructor by ID: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getTaskById(taskId: Long, onResult: (PeclTaskEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val task = repository.getTaskById(taskId)
                onResult(task)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting task by ID: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getPeclStudentById(studentId: Long, onResult: (PeclStudentEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val student = repository.getPeclStudentById(studentId).first()
                onResult(student)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting student by ID: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getScaleByName(name: String): ScaleEntity? {
        var scale: ScaleEntity? = null
        viewModelScope.launch {
            try {
                scale = repository.getScaleByName(name)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting scale by name: ${e.message}", e)
            }
        }
        return scale
    }

    fun loadQuestionsForTask(taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForTask(taskId).first()
                _questionsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading questions for task: ${e.message}", e)
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
                Log.e("PeclViewModel", "Error loading questions for POI: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions"))
            }
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

    fun getTaskGradeForStudent(studentId: Long, taskId: Long): Flow<Double?> {
        return repository.getTaskGradeForStudent(studentId, taskId)
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.insertEvaluationResult(result)) {
                    is AppResult.Success -> {
                        loadEvaluationResultsForStudent(result.student_id)
                    }
                    is AppResult.Error -> {
                        _evaluationResultsState.postValue(AppState.Error(appResult.message))
                    }
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting evaluation result: ${e.message}", e)
                _evaluationResultsState.postValue(AppState.Error(e.message ?: "Error inserting evaluation result"))
            }
        }
    }

    fun deleteEvaluationsForStudentAndTask(studentId: Long, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteEvaluationsForStudentAndTask(studentId, taskId)) {
                    is AppResult.Success -> {
                        loadEvaluationsForStudentAndTask(studentId, taskId)
                    }
                    is AppResult.Error -> {
                        _evaluationsForStudentAndTaskState.postValue(AppState.Error(result.message))
                    }
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error deleting evaluations: ${e.message}", e)
                _evaluationsForStudentAndTaskState.postValue(AppState.Error(e.message ?: "Error deleting evaluations"))
            }
        }
    }

    fun insertPeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertPeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> _studentsState.postValue(AppState.Error(result.message))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting student: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error inserting student"))
            }
        }
    }

    fun updatePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.updatePeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> _studentsState.postValue(AppState.Error(result.message))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error updating student: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error updating student"))
            }
        }
    }

    fun deletePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deletePeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> _studentsState.postValue(AppState.Error(result.message))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error deleting student: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error deleting student"))
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

    fun getTaskForQuestion(questionId: Long, onResult: (PeclTaskEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val taskId = repository.getTaskIdForQuestion(questionId)
                if (taskId != null) {
                    val task = repository.getTaskById(taskId)
                    onResult(task)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting task for question: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun insertQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertQuestion(question, taskId)) {
                    is AppResult.Success -> loadAllQuestions()
                    is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error inserting question"))
            }
        }
    }

    fun updateQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateQuestion(question, taskId)) {
                    is AppResult.Success -> loadAllQuestions()
                    is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error updating question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error updating question"))
            }
        }
    }

    fun deleteQuestion(question: PeclQuestionEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteQuestion(question)) {
                    is AppResult.Success -> loadAllQuestions()
                    is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error deleting question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error deleting question"))
            }
        }
    }

    fun loadAllQuestions() {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllQuestions().first()
                _questionsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading all questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions"))
            }
        }
    }
}