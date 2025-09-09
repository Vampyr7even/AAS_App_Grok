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
class AdminViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _scalesState = MutableLiveData<State<List<ScaleEntity>>>(State.Success(emptyList()))
    val scalesState: LiveData<State<List<ScaleEntity>>> = _scalesState

    private val _scaleState = MutableLiveData<State<ScaleEntity?>>(State.Success(null))
    val scaleState: LiveData<State<ScaleEntity?>> = _scaleState

    private val _programsState = MutableLiveData<State<List<PeclProgramEntity>>>(State.Success(emptyList()))
    val programsState: LiveData<State<List<PeclProgramEntity>>> = _programsState

    private val _poisState = MutableLiveData<State<List<PeclPoiEntity>>>(State.Success(emptyList()))
    val poisState: LiveData<State<List<PeclPoiEntity>>> = _poisState

    private val _tasksState = MutableLiveData<State<List<PeclTaskEntity>>>(State.Success(emptyList()))
    val tasksState: LiveData<State<List<PeclTaskEntity>>> = _tasksState

    private val _taskState = MutableLiveData<State<PeclTaskEntity?>>(State.Success(null))
    val taskState: LiveData<State<PeclTaskEntity?>> = _taskState

    private val _questionsState = MutableLiveData<State<List<QuestionWithTask>>>(State.Success(emptyList()))
    val questionsState: LiveData<State<List<QuestionWithTask>>> = _questionsState

    private val _poisSimple = MutableLiveData<List<PeclPoiEntity>>(emptyList())
    val poisSimple: LiveData<List<PeclPoiEntity>> = _poisSimple

    fun getScaleById(scaleId: Long) {
        _scaleState.value = State.Loading
        viewModelScope.launch {
            try {
                val scale = repository.getScaleById(scaleId).first()
                _scaleState.postValue(State.Success(scale))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading scale by ID: ${e.message}", e)
                _scaleState.postValue(State.Error(e.message ?: "Error loading scale"))
            }
        }
    }

    fun insertScale(scale: ScaleEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.insertScale(scale)) {
                    is AppResult.Success -> loadScales()
                    is AppResult.Error -> _scalesState.postValue(State.Error(appResult.exception.message ?: "Error inserting scale"))
                    else -> _scalesState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting scale: ${e.message}", e)
                _scalesState.postValue(State.Error(e.message ?: "Error inserting scale"))
            }
        }
    }

    fun loadScales() {
        _scalesState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllScales().first()
                _scalesState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading scales: ${e.message}", e)
                _scalesState.postValue(State.Error(e.message ?: "Error loading scales"))
            }
        }
    }

    fun updateScale(scale: ScaleEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.updateScale(scale)) {
                    is AppResult.Success -> loadScales()
                    is AppResult.Error -> _scalesState.postValue(State.Error(appResult.exception.message ?: "Error updating scale"))
                    else -> _scalesState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating scale: ${e.message}", e)
                _scalesState.postValue(State.Error(e.message ?: "Error updating scale"))
            }
        }
    }

    fun deleteScale(scale: ScaleEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.deleteScale(scale)) {
                    is AppResult.Success -> loadScales()
                    is AppResult.Error -> _scalesState.postValue(State.Error(appResult.exception.message ?: "Error deleting scale"))
                    else -> _scalesState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting scale: ${e.message}", e)
                _scalesState.postValue(State.Error(e.message ?: "Error deleting scale"))
            }
        }
    }

    fun insertProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.insertProgram(program)) {
                    is AppResult.Success -> loadPrograms()
                    is AppResult.Error -> _programsState.postValue(State.Error(appResult.exception.message ?: "Error inserting program"))
                    else -> _programsState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting program: ${e.message}", e)
                _programsState.postValue(State.Error(e.message ?: "Error inserting program"))
            }
        }
    }

    fun loadPrograms() {
        _programsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPrograms().first()
                _programsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading programs: ${e.message}", e)
                _programsState.postValue(State.Error(e.message ?: "Error loading programs"))
            }
        }
    }

    fun updateProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.updateProgram(program)) {
                    is AppResult.Success -> loadPrograms()
                    is AppResult.Error -> _programsState.postValue(State.Error(appResult.exception.message ?: "Error updating program"))
                    else -> _programsState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating program: ${e.message}", e)
                _programsState.postValue(State.Error(e.message ?: "Error updating program"))
            }
        }
    }

    fun deleteProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.deleteProgram(program)) {
                    is AppResult.Success -> loadPrograms()
                    is AppResult.Error -> _programsState.postValue(State.Error(appResult.exception.message ?: "Error deleting program"))
                    else -> _programsState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting program: ${e.message}", e)
                _programsState.postValue(State.Error(e.message ?: "Error deleting program"))
            }
        }
    }

    fun loadTasksForPoi(poiId: Long) {
        _tasksState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getTasksForPoi(poiId).first()
                _tasksState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading tasks for POI: ${e.message}", e)
                _tasksState.postValue(State.Error(e.message ?: "Error loading tasks"))
            }
        }
    }

    fun getTaskById(taskId: Long) {
        _taskState.value = State.Loading
        viewModelScope.launch {
            try {
                val task = repository.getTaskById(taskId)
                _taskState.postValue(State.Success(task))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading task by ID: ${e.message}", e)
                _taskState.postValue(State.Error(e.message ?: "Error loading task"))
            }
        }
    }

    fun loadQuestionsForTask(taskId: Long) {
        _questionsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForTask(taskId).first()
                _questionsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading questions for task: ${e.message}", e)
                _questionsState.postValue(State.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun insertQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.insertQuestion(question, taskId)) {
                    is AppResult.Success -> loadQuestionsForTask(taskId)
                    is AppResult.Error -> _questionsState.postValue(State.Error(appResult.exception.message ?: "Error inserting question"))
                    else -> _questionsState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting question: ${e.message}", e)
                _questionsState.postValue(State.Error(e.message ?: "Error inserting question"))
            }
        }
    }

    fun updateQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.updateQuestion(question, taskId)) {
                    is AppResult.Success -> loadQuestionsForTask(taskId)
                    is AppResult.Error -> _questionsState.postValue(State.Error(appResult.exception.message ?: "Error updating question"))
                    else -> _questionsState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating question: ${e.message}", e)
                _questionsState.postValue(State.Error(e.message ?: "Error updating question"))
            }
        }
    }

    fun deleteQuestion(question: PeclQuestionEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.deleteQuestion(question)) {
                    is AppResult.Success -> loadAllQuestions()
                    is AppResult.Error -> _questionsState.postValue(State.Error(appResult.exception.message ?: "Error deleting question"))
                    else -> _questionsState.postValue(State.Error("Unexpected result"))
                }
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting question: ${e.message}", e)
                _questionsState.postValue(State.Error(e.message ?: "Error deleting question"))
            }
        }
    }

    fun loadAllQuestions() {
        _questionsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllQuestionsWithTasks().first()
                _questionsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading all questions: ${e.message}", e)
                _questionsState.postValue(State.Error(e.message ?: "Error loading questions"))
            }
        }
    }
}