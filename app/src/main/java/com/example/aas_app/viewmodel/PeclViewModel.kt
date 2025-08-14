package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.entity.PeclQuestionEntity
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PeclViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _questionsState = MutableLiveData<AppState<List<PeclQuestionEntity>>>()
    val questionsState: LiveData<AppState<List<PeclQuestionEntity>>> = _questionsState

    private val _evaluationResultsState = MutableLiveData<AppState<List<PeclEvaluationResultEntity>>>()
    val evaluationResultsState: LiveData<AppState<List<PeclEvaluationResultEntity>>> = _evaluationResultsState

    private val _averageScoreState = MutableLiveData<AppState<Float>>()
    val averageScoreState: LiveData<AppState<Float>> = _averageScoreState

    private val _tasksState = MutableLiveData<AppState<List<PeclTaskEntity>>>()
    val tasksState: LiveData<AppState<List<PeclTaskEntity>>> = _tasksState

    private val _studentsState = MutableLiveData<AppState<List<UserEntity>>>()
    val studentsState: LiveData<AppState<List<UserEntity>>> = _studentsState

    private val _commentsState = MutableLiveData<AppState<List<String>>>()
    val commentsState: LiveData<AppState<List<String>>> = _commentsState

    fun loadQuestionsForPoi(program: String, poi: String) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForPoi(program, poi).first()
                _questionsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions for POI"))
            }
        }
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            val insertResult = repository.insertEvaluationResult(result)
            // Handle result if needed
        }
    }

    fun getAverageScoreForStudent(studentId: Long, poiId: Long) {
        _averageScoreState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val average = repository.getAverageScoreForStudent(studentId, poiId)
                _averageScoreState.postValue(AppState.Success(average))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error calculating average score: ${e.message}", e)
                _averageScoreState.postValue(AppState.Error(e.message ?: "Error calculating average score"))
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
                _tasksState.postValue(AppState.Error(e.message ?: "Error loading tasks for POI"))
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
                Log.e("PeclViewModel", "Error loading questions for task: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions for task"))
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
                _studentsState.postValue(AppState.Error(e.message ?: "Error loading students for program"))
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
                Log.e("PeclViewModel", "Error loading evaluation results for student: ${e.message}", e)
                _evaluationResultsState.postValue(AppState.Error(e.message ?: "Error loading evaluation results for student"))
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
                Log.e("PeclViewModel", "Error loading comments for student: ${e.message}", e)
                _commentsState.postValue(AppState.Error(e.message ?: "Error loading comments for student"))
            }
        }
    }

    // Add other aggregate methods as needed, e.g., getCommentsForStudent
}