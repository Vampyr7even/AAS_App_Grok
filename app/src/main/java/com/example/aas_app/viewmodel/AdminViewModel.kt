package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.RoomSQLException
import com.example.aas_app.data.entities.PeclEvaluationResultEntity
import com.example.aas_app.data.entities.PeclPoiEntity
import com.example.aas_app.data.entities.PeclProgramEntity
import com.example.aas_app.data.entities.PeclQuestionEntity
import com.example.aas_app.data.entities.PeclScaleEntity
import com.example.aas_app.data.entities.PeclTaskEntity
import com.example.aas_app.data.entities.UserEntity
import com.example.aas_app.data.repository.AppRepository
import com.example.aas_app.util.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _questionsState = MutableLiveData<AppState<List<PeclQuestionEntity>>>()
    val questionsState: LiveData<AppState<List<PeclQuestionEntity>>> = _questionsState

    private val _scalesState = MutableLiveData<AppState<List<PeclScaleEntity>>>()
    val scalesState: LiveData<AppState<List<PeclScaleEntity>>> = _scalesState

    private val _studentsState = MutableLiveData<AppState<List<UserEntity>>>()
    val studentsState: LiveData<AppState<List<UserEntity>>> = _studentsState

    fun loadPrograms() {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPrograms()
                _programsState.postValue(AppState.Success(data))
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error loading programs: ${e.message}", e)
                _programsState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading programs: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error loading programs"))
            }
        }
    }

    fun loadPoisForProgram(programId: Long) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getPoisForProgram(programId)
                _poisState.postValue(AppState.Success(data))
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error loading POIs: ${e.message}", e)
                _poisState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
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
                val data = repository.getTasksForPoi(poiId)
                _tasksState.postValue(AppState.Success(data))
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error loading tasks: ${e.message}", e)
                _tasksState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
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
                val data = repository.getQuestionsForTask(taskId)
                _questionsState.postValue(AppState.Success(data))
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun loadScales() {
        _scalesState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllScales()
                _scalesState.postValue(AppState.Success(data))
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error loading scales: ${e.message}", e)
                _scalesState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading scales: ${e.message}", e)
                _scalesState.postValue(AppState.Error(e.message ?: "Error loading scales"))
            }
        }
    }

    fun loadQuestionsByIds(ids: List<Long>) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsByIds(ids)
                _questionsState.postValue(AppState.Success(data))
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error loading questions by IDs: ${e.message}", e)
                _questionsState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading questions by IDs: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions by IDs"))
            }
        }
    }

    fun loadStudentsForProgram(programId: Long) {
        _studentsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForProgram(programId)
                _studentsState.postValue(AppState.Success(data))
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error loading students: ${e.message}", e)
                _studentsState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading students: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error loading students for program"))
            }
        }
    }

    fun insertProgram(program: PeclProgramEntity) {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.insertProgram(program)
                loadPrograms()
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error inserting program: ${e.message}", e)
                _programsState.postValue(AppState.Error("Database insertion error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting program: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error inserting program"))
            }
        }
    }

    fun updateProgram(program: PeclProgramEntity) {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.updateProgram(program)
                loadPrograms()
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error updating program: ${e.message}", e)
                _programsState.postValue(AppState.Error("Database update error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating program: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error updating program"))
            }
        }
    }

    fun deleteProgram(program: PeclProgramEntity) {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.deleteProgram(program)
                loadPrograms()
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error deleting program: ${e.message}", e)
                _programsState.postValue(AppState.Error("Cannot delete program - referenced elsewhere (e.g., POIs assigned)"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting program: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error deleting program"))
            }
        }
    }

    fun insertPoi(poi: PeclPoiEntity) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.insertPoi(poi)
                loadPoisForProgram(poi.programId)
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error inserting POI: ${e.message}", e)
                _poisState.postValue(AppState.Error("Database insertion error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting POI: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error inserting POI"))
            }
        }
    }

    fun updatePoi(poi: PeclPoiEntity) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.updatePoi(poi)
                loadPoisForProgram(poi.programId)
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error updating POI: ${e.message}", e)
                _poisState.postValue(AppState.Error("Database update error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating POI: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error updating POI"))
            }
        }
    }

    fun deletePoi(poi: PeclPoiEntity) {
        _poisState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.deletePoi(poi)
                loadPoisForProgram(poi.programId)
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error deleting POI: ${e.message}", e)
                _poisState.postValue(AppState.Error("Cannot delete POI - tasks assigned"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting POI: ${e.message}", e)
                _poisState.postValue(AppState.Error(e.message ?: "Error deleting POI"))
            }
        }
    }

    fun insertQuestion(question: PeclQuestionEntity, taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.insertQuestionWithAssignment(question, taskId)
                loadQuestionsForTask(taskId)
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error inserting question: ${e.message}", e)
                _questionsState.postValue(AppState.Error("Database insertion error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error inserting question"))
            }
        }
    }

    fun updateQuestion(question: PeclQuestionEntity, taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.updateQuestion(question)
                loadQuestionsForTask(taskId)
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error updating question: ${e.message}", e)
                _questionsState.postValue(AppState.Error("Database update error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error updating question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error updating question"))
            }
        }
    }

    fun deleteQuestion(question: PeclQuestionEntity, taskId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                repository.deleteQuestion(question)
                loadQuestionsForTask(taskId)
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error deleting question: ${e.message}", e)
                _questionsState.postValue(AppState.Error("Cannot delete question - referenced elsewhere (e.g., evaluations)"))
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error deleting question: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error deleting question"))
            }
        }
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            try {
                repository.insertEvaluationResult(result)
                // Optionally post to a dedicated state if added for evaluations
            } catch (e: RoomSQLException) {
                Log.e("AdminViewModel", "Database error inserting evaluation result: ${e.message}", e)
                // Handle via UI feedback
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error inserting evaluation result: ${e.message}", e)
                // Handle via UI feedback
            }
        }
    }
}