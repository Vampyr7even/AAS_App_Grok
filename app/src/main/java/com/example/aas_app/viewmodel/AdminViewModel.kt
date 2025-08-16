package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppResult
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.ScaleEntity
import com.example.aas_app.data.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _programsState = MutableLiveData<AppState<List<PeclProgramEntity>>>()
    val programsState: LiveData<AppState<List<PeclProgramEntity>>> = _programsState

    private val _poisState = MutableLiveData<AppState<List<PoiWithPrograms>>>()
    val poisState: LiveData<AppState<List<PoiWithPrograms>>> = _poisState

    private val _tasksState = MutableLiveData<AppState<List<TaskWithPois>>>()
    val tasksState: LiveData<AppState<List<TaskWithPois>>> = _tasksState

    private val _questionsState = MutableLiveData<AppState<List<PeclQuestionEntity>>>()
    val questionsState: LiveData<AppState<List<PeclQuestionEntity>>> = _questionsState

    private val _scalesState = MutableLiveData<AppState<List<ScaleEntity>>>()
    val scalesState: LiveData<AppState<List<ScaleEntity>>> = _scalesState

    private val _studentsState = MutableLiveData<AppState<List<UserEntity>>>()
    val studentsState: LiveData<AppState<List<UserEntity>>> = _studentsState

    private val _poisSimple = MutableLiveData<List<PeclPoiEntity>>()
    val poisSimple: LiveData<List<PeclPoiEntity>> = _poisSimple

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

    fun loadAllPoisWithPrograms() {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val pois = repository.getAllPois().first()
                val poisWithPrograms = pois.map { poi ->
                    val programs = repository.getProgramsForPoi(poi.id).first()
                    PoiWithPrograms(poi, programs)
                }
                _poisState.postValue(AppState.Success(poisWithPrograms))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading POIs with programs: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error loading POIs with programs"))
            }
        }
    }

    fun loadAllPois() {
        viewModelScope.launch {
            try {
                val data = repository.getAllPois().first()
                _poisSimple.postValue(data)
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading simple POIs: ${e.message}", e)
            }
        }
    }

    fun loadAllTasksWithPois() {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val tasks = repository.getAllTasks().first()
                val tasksWithPois = tasks.map { task ->
                    val pois = repository.getPoisForTask(task.id).first()
                    TaskWithPois(task, pois)
                }
                _tasksState.postValue(AppState.Success(tasksWithPois))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading tasks with POIs: ${e.message}", e)
                _tasksState.postValue(AppState.Error(e.message ?: "Error loading tasks with POIs"))
            }
        }
    }

    fun loadPoisForProgram(programId: Long) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getPoisForProgram(programId).first()
                _poisState.postValue(AppState.Success(data.map { PoiWithPrograms(it, emptyList()) })) // Adjust if needed
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading POIs: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error loading POIs"))
            }
        }
    }

    fun loadTasksForPoi(poiId: Long) {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getTasksForPoi(poiId).first()
                _tasksState.postValue(AppState.Success(data.map { TaskWithPois(it, emptyList()) })) // Adjust if needed
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading tasks: ${e.message}", e)
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
                Log.e("AdminViewModel", "Error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions for task"))
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
                Log.e("AdminViewModel", "Error loading all questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading all questions"))
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

    fun loadStudentsForProgram(programId: Long) {
        _studentsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForProgram(programId).first()
                _studentsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading students: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error loading students for program"))
            }
        }
    }

    suspend fun getQuestionById(id: Long): PeclQuestionEntity? = repository.getQuestionById(id)

    suspend fun getScaleById(id: Long): ScaleEntity? = repository.getScaleById(id)

    suspend fun getTaskById(id: Long): PeclTaskEntity? = repository.getTaskById(id)

    fun insertProgram(program: PeclProgramEntity) {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.insertProgram(program)
            when (result) {
                is AppResult.Success -> loadPrograms()
                is AppResult.Error -> _programsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun updateProgram(program: PeclProgramEntity) {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.updateProgram(program)
            when (result) {
                is AppResult.Success -> loadPrograms()
                is AppResult.Error -> _programsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun deleteProgram(program: PeclProgramEntity) {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.deleteProgram(program)
            when (result) {
                is AppResult.Success -> loadPrograms()
                is AppResult.Error -> _programsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun insertPoi(poi: PeclPoiEntity, programIds: List<Long>) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.insertPoi(poi, programIds)
            when (result) {
                is AppResult.Success -> loadAllPoisWithPrograms()
                is AppResult.Error -> _poisState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun updatePoi(poi: PeclPoiEntity, programIds: List<Long>) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.updatePoi(poi, programIds)
            when (result) {
                is AppResult.Success -> loadAllPoisWithPrograms()
                is AppResult.Error -> _poisState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun deletePoi(poi: PeclPoiEntity) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.deletePoi(poi)
            when (result) {
                is AppResult.Success -> loadAllPoisWithPrograms()
                is AppResult.Error -> _poisState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun insertTask(task: PeclTaskEntity, poiIds: List<Long>) {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.insertTask(task, poiIds)
            when (result) {
                is AppResult.Success -> loadAllTasksWithPois()
                is AppResult.Error -> _tasksState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun updateTask(task: PeclTaskEntity, poiIds: List<Long>? = null) {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.updateTask(task, poiIds)
            when (result) {
                is AppResult.Success -> loadAllTasksWithPois()
                is AppResult.Error -> _tasksState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun deleteTask(task: PeclTaskEntity) {
        _tasksState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.deleteTask(task)
            when (result) {
                is AppResult.Success -> loadAllTasksWithPois()
                is AppResult.Error -> _tasksState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun insertQuestion(question: PeclQuestionEntity, taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.insertQuestionWithAssignment(question, taskId)
            when (result) {
                is AppResult.Success -> loadQuestionsForTask(taskId)
                is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun updateQuestion(question: PeclQuestionEntity, taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.updateQuestion(question)
            when (result) {
                is AppResult.Success -> loadQuestionsForTask(taskId)
                is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun deleteQuestion(question: PeclQuestionEntity, taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.deleteQuestion(question)
            when (result) {
                is AppResult.Success -> loadQuestionsForTask(taskId)
                is AppResult.Error -> _questionsState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun insertScale(scale: ScaleEntity) {
        _scalesState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.insertScale(scale)
            when (result) {
                is AppResult.Success -> loadScales()
                is AppResult.Error -> _scalesState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun updateScale(scale: ScaleEntity) {
        _scalesState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.updateScale(scale)
            when (result) {
                is AppResult.Success -> loadScales()
                is AppResult.Error -> _scalesState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun deleteScale(scale: ScaleEntity) {
        _scalesState.value = AppState.Loading
        viewModelScope.launch {
            val result = repository.deleteScale(scale)
            when (result) {
                is AppResult.Success -> loadScales()
                is AppResult.Error -> _scalesState.postValue(AppState.Error(result.message))
            }
        }
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            val insertResult = repository.insertEvaluationResult(result)
            // Handle result if needed
        }
    }

    fun loadQuestionsForPoi(poiId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForPoi(poiId).first()
                _questionsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading questions for POI: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions for POI"))
            }
        }
    }
}

data class PoiWithPrograms(
    val poi: PeclPoiEntity,
    val programs: List<String>
)

data class TaskWithPois(
    val task: PeclTaskEntity,
    val pois: List<String>
)