package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclPoiEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclStudentEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

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
            repository.insertEvaluationResult(result)
            // Reload if needed
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
                val data = repository.getEvaluationResultsForStudent(studentId).first().map { it.comment }
                _commentsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading comments: ${e.message}", e)
                _commentsState.postValue(AppState.Error(e.message ?: "Error loading comments"))
            }
        }
    }

    suspend fun getAverageScoreForStudent(studentId: Long, poiId: Long): Double {
        return try {
            repository.getAverageScoreForStudent(studentId, poiId)
        } catch (e: Exception) {
            Log.e("PeclViewModel", "Error getting average score: ${e.message}", e)
            0.0
        }
    }

    fun loadStudentsForProgram(programId: Long) {
        _studentsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForProgram(programId).first()
                _studentsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading students: ${e.message}", e)
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
        viewModelScope.launch {
            try {
                repository.insertPeclStudent(student)
                loadStudents()
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting student: ${e.message}", e)
            }
        }
    }

    fun updatePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                repository.updatePeclStudent(student)
                loadStudents()
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error updating student: ${e.message}", e)
            }
        }
    }

    fun deletePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                repository.deletePeclStudent(student)
                loadStudents()
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error deleting student: ${e.message}", e)
            }
        }
    }
}