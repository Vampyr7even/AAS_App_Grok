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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _programsState = MutableLiveData<AppState<List<PeclProgramEntity>>>()
    val programsState: LiveData<AppState<List<PeclProgramEntity>>> = _programsState

    private val _poisState = MutableLiveData<AppState<List<PeclPoiEntity>>>()
    val poisState: LiveData<AppState<List<PeclPoiEntity>>> = _poisState

    private val _tasksState = MutableLiveData<AppState<List<PeclTaskEntity>>>()
    val tasksState: LiveData<AppState<List<PeclTaskEntity>>> = _tasksState

    private val _questionsState = MutableLiveData<AppState<List<QuestionWithTask>>>()
    val questionsState: LiveData<AppState<List<QuestionWithTask>>> = _questionsState

    private val _scalesState = MutableLiveData<AppState<List<ScaleEntity>>>()
    val scalesState: LiveData<AppState<List<ScaleEntity>>> = _scalesState

    fun loadPrograms() {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPrograms().first()
                _programsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading programs: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error loading programs"))
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
                Log.e("AdminViewModel", "Error loading POIs: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error loading POIs"))
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
                Log.e("AdminViewModel", "Error loading POIs for program: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error loading POIs"))
            }
        }
    }

    fun loadTasks() {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllTasks().first()
                _tasksState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading tasks: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error loading tasks"))
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
                Log.e("AdminViewModel", "Error loading tasks for POI: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error loading tasks"))
            }
        }
    }

    fun loadQuestions() {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllQuestionsWithTasks().first().sortedBy { qwt: QuestionWithTask -> qwt.question.subTask }
                _questionsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun loadQuestionsForTask(taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForTask(taskId).first()
                _questionsState.postValue(AppState.Success(data.map { QuestionWithTask(it, null) }))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading questions for task: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun loadScales() {
        _scalesState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllScales().first()
                _scalesState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading scales: ${e.message}", e)
                _scalesState.postValue(AppState.Error(e.message ?: "Error loading scales"))
            }
        }
    }

    fun insertProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertProgram(program)) {
                    is AppResult.Success -> loadPrograms()
                    is AppResult.Error -> _programsState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting program: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error inserting program"))
            }
        }
    }

    fun updateProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateProgram(program)) {
                    is AppResult.Success -> loadPrograms()
                    is AppResult.Error -> _programsState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating program: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error updating program"))
            }
        }
    }

    fun deleteProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteProgram(program)) {
                    is AppResult.Success -> loadPrograms()
                    is AppResult.Error -> _programsState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting program: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error deleting program"))
            }
        }
    }

    fun insertPoi(poi: PeclPoiEntity, programIds: List<Long>) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertPoi(poi, programIds)) {
                    is AppResult.Success -> loadPois()
                    is AppResult.Error -> _poisState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting POI: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error inserting POI"))
            }
        }
    }

    fun updatePoi(poi: PeclPoiEntity, programIds: List<Long>) {
        viewModelScope.launch {
            try {
                when (val result = repository.updatePoi(poi, programIds)) {
                    is AppResult.Success -> loadPois()
                    is AppResult.Error -> _poisState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating POI: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error updating POI"))
            }
        }
    }

    fun deletePoi(poi: PeclPoiEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deletePoi(poi)) {
                    is AppResult.Success -> loadPois()
                    is AppResult.Error -> _poisState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting POI: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error deleting POI"))
            }
        }
    }

    fun insertTask(task: PeclTaskEntity, poiIds: List<Long>) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertTask(task, poiIds)) {
                    is AppResult.Success -> loadTasks()
                    is AppResult.Error -> _tasksState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting task: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error inserting task"))
            }
        }
    }

    fun updateTask(task: PeclTaskEntity, poiIds: List<Long>?) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateTask(task, poiIds)) {
                    is AppResult.Success -> loadTasks()
                    is AppResult.Error -> _tasksState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating task: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error updating task"))
            }
        }
    }

    fun deleteTask(task: PeclTaskEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteTask(task)) {
                    is AppResult.Success -> loadTasks()
                    is AppResult.Error -> _tasksState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting task: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error deleting task"))
            }
        }
    }

    fun insertQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertQuestion(question, taskId)) {
                    is AppResult.Success -> loadQuestions()
                    is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error inserting question"))
            }
        }
    }

    fun updateQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateQuestion(question, taskId)) {
                    is AppResult.Success -> loadQuestions()
                    is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error updating question"))
            }
        }
    }

    fun deleteQuestion(question: PeclQuestionEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteQuestion(question)) {
                    is AppResult.Success -> loadQuestions()
                    is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error deleting question"))
            }
        }
    }

    fun insertScale(scale: ScaleEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertScale(scale)) {
                    is AppResult.Success -> loadScales()
                    is AppResult.Error -> _scalesState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting scale: ${e.message}", e)
                _scalesState.postValue(AppState.Error(e.message ?: "Error inserting scale"))
            }
        }
    }

    fun updateScale(scale: ScaleEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateScale(scale)) {
                    is AppResult.Success -> loadScales()
                    is AppResult.Error -> _scalesState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating scale: ${e.message}", e)
                _scalesState.postValue(AppState.Error(e.message ?: "Error updating scale"))
            }
        }
    }

    fun deleteScale(scale: ScaleEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteScale(scale)) {
                    is AppResult.Success -> loadScales()
                    is AppResult.Error -> _scalesState.postValue(AppState.Error(result.message))
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting scale: ${e.message}", e)
                _scalesState.postValue(AppState.Error(e.message ?: "Error deleting scale"))
            }
        }
    }

    fun getProgramsForPoi(poiId: Long) {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getProgramsForPoi(poiId).first()
                _programsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading programs for POI: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error loading programs for POI"))
            }
        }
    }

    fun getPoisForTask(taskId: Long) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getPoisForTask(taskId).first()
                _poisState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading POIs for task: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error loading POIs for task"))
            }
        }
    }

    suspend fun getQuestionById(id: Long): PeclQuestionEntity? {
        return try {
            repository.getQuestionById(id)
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Error getting question by ID: ${e.message}", e)
            null
        }
    }

    suspend fun getScaleById(id: Long): ScaleEntity? {
        return try {
            repository.getScaleById(id)
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Error getting scale by ID: ${e.message}", e)
            null
        }
    }

    suspend fun getTaskById(id: Long): PeclTaskEntity? {
        return try {
            repository.getTaskById(id)
        } catch (e: Exception) {
            Log.e("AdminViewModel", "Error getting task by ID: ${e.message}", e)
            null
        }
    }
}