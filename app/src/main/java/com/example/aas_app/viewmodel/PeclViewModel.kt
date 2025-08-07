package com.example.aas_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.Result
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class PeclViewModel(private val repository: AppRepository) : ViewModel() {

    private val _peclPrograms = MutableStateFlow<List<PeclProgramEntity>>(emptyList())
    val peclPrograms: StateFlow<List<PeclProgramEntity>> = _peclPrograms

    private val _peclPois = MutableStateFlow<List<PeclPoiEntity>>(emptyList())
    val peclPois: StateFlow<List<PeclPoiEntity>> = _peclPois

    private val _peclQuestions = MutableStateFlow<List<PeclQuestionEntity>>(emptyList())
    val peclQuestions: StateFlow<List<PeclQuestionEntity>> = _peclQuestions

    private val _questionsForPoi = MutableStateFlow<List<PeclQuestionEntity>>(emptyList())
    val questionsForPoi: StateFlow<List<PeclQuestionEntity>> = _questionsForPoi

    private val _scales = MutableStateFlow<List<ScaleEntity>>(emptyList())
    val scales: StateFlow<List<ScaleEntity>> = _scales

    private val _peclTasks = MutableStateFlow<List<PeclTaskEntity>>(emptyList())
    val peclTasks: StateFlow<List<PeclTaskEntity>> = _peclTasks

    private val _evaluationResults = MutableStateFlow<List<PeclEvaluationResultEntity>>(emptyList())
    val evaluationResults: StateFlow<List<PeclEvaluationResultEntity>> = _evaluationResults

    private val _state = MutableLiveData<State<Any>>()
    val state: LiveData<State<Any>> = _state

    init {
        loadPeclPrograms()
        loadPeclPois()
        loadPeclQuestions()
        loadScales()
        loadPeclTasks()
    }

    fun loadPeclPrograms() {
        viewModelScope.launch {
            _state.postValue(State.Loading)
            val result = repository.getAllPeclPrograms()
            if (result is Result.Success) {
                _peclPrograms.value = result.data
                if (_peclPrograms.value.isEmpty()) {
                    _state.postValue(State.Error("No programs found"))
                } else {
                    _state.postValue(State.Success(result.data))
                }
            } else {
                _state.postValue(State.Error("Failed to load programs: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadPeclPois() {
        viewModelScope.launch {
            val result = repository.getAllPeclPois()
            if (result is Result.Success) {
                _peclPois.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load POIs: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadPeclQuestions() {
        viewModelScope.launch {
            val result = repository.getAllPeclQuestions()
            if (result is Result.Success) {
                _peclQuestions.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load questions: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadQuestionsForPoi(program: String, poi: String) {
        viewModelScope.launch {
            val result = repository.getQuestionsForPoi(program, poi)
            if (result is Result.Success) {
                _questionsForPoi.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load questions for POI: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadScales() {
        viewModelScope.launch {
            val result = repository.getAllScales()
            if (result is Result.Success) {
                _scales.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load scales: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadPeclTasks() {
        viewModelScope.launch {
            val result = repository.getAllPeclTasks()
            if (result is Result.Success) {
                _peclTasks.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load tasks: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadEvaluationResults(program: String, poi: String) {
        viewModelScope.launch {
            val result = repository.getPeclEvaluationResults(program, poi)
            if (result is Result.Success) {
                _evaluationResults.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load evaluation results: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun insertQuestionWithAssignment(question: PeclQuestionEntity, taskId: Int) {
        viewModelScope.launch {
            val result = repository.insertQuestionWithAssignment(question, taskId)
            if (result is Result.Success) {
                loadPeclQuestions()
            } else {
                _state.postValue(State.Error("Failed to insert question: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deletePeclPoi(poi: PeclPoiEntity) {
        viewModelScope.launch {
            val result = repository.deletePeclPoi(poi)
            if (result is Result.Success) {
                loadPeclPois()
            } else {
                _state.postValue(State.Error("Failed to delete POI: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun updatePeclProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            val result = repository.updatePeclProgram(program)
            if (result is Result.Success) {
                loadPeclPrograms()
            } else {
                _state.postValue(State.Error("Failed to update program: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deletePeclProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            val result = repository.deletePeclProgram(program)
            if (result is Result.Success) {
                loadPeclPrograms()
            } else {
                _state.postValue(State.Error("Failed to delete program: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun updatePeclQuestion(question: PeclQuestionEntity) {
        viewModelScope.launch {
            val result = repository.updatePeclQuestion(question)
            if (result is Result.Success) {
                loadPeclQuestions()
            } else {
                _state.postValue(State.Error("Failed to update question: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deleteScale(scale: ScaleEntity) {
        viewModelScope.launch {
            val result = repository.deleteScale(scale)
            if (result is Result.Success) {
                loadScales()
            } else {
                _state.postValue(State.Error("Failed to delete scale: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun updatePeclTask(task: PeclTaskEntity) {
        viewModelScope.launch {
            val result = repository.updatePeclTask(task)
            if (result is Result.Success) {
                loadPeclTasks()
            } else {
                _state.postValue(State.Error("Failed to update task: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deletePeclTask(task: PeclTaskEntity) {
        viewModelScope.launch {
            val result = repository.deletePeclTask(task)
            if (result is Result.Success) {
                loadPeclTasks()
            } else {
                _state.postValue(State.Error("Failed to delete task: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    suspend fun getPeclQuestionById(id: Int): Result<PeclQuestionEntity?> = repository.getPeclQuestionById(id)

    suspend fun getScaleById(id: Int): Result<ScaleEntity?> = repository.getScaleById(id)

    suspend fun getPeclTaskById(id: Int): Result<PeclTaskEntity?> = repository.getPeclTaskById(id)

    suspend fun getPeclProgramById(id: Int): Result<PeclProgramEntity?> = repository.getPeclProgramById(id)

    fun insertPeclEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            val insertResult = repository.insertPeclEvaluationResult(result)
            if (insertResult is Result.Success) {
                // Refresh
            } else {
                _state.postValue(State.Error("Insert failed: ${(insertResult as Result.Error).exception.message}"))
            }
        }
    }

    fun insertScale(scale: ScaleEntity) {
        viewModelScope.launch {
            val result = repository.insertScale(scale)
            if (result is Result.Success) {
                loadScales()
            } else {
                _state.postValue(State.Error("Failed to insert scale: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun updateScale(scale: ScaleEntity) {
        viewModelScope.launch {
            val result = repository.updateScale(scale)
            if (result is Result.Success) {
                loadScales()
            } else {
                _state.postValue(State.Error("Failed to update scale: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deletePeclQuestion(question: PeclQuestionEntity) {
        viewModelScope.launch {
            val result = repository.deletePeclQuestion(question)
            if (result is Result.Success) {
                loadPeclQuestions()
            } else {
                _state.postValue(State.Error("Delete failed: referenced elsewhere? ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun getAverageScorePerStudent(studentId: Int, poi: String): StateFlow<State<Double>> {
        val average = MutableStateFlow<State<Double>>(State.Loading)
        viewModelScope.launch {
            val result = repository.getAverageScorePerStudent(studentId, poi)
            if (result is Result.Success) {
                average.value = State.Success(result.data)
            } else {
                average.value = State.Error("Failed to get average: ${(result as Result.Error).exception.message}")
            }
        }
        return average
    }
}