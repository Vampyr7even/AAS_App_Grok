package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.RoomSQLException
import com.example.aas_app.data.entities.PeclEvaluationResultEntity
import com.example.aas_app.data.entities.PeclQuestionEntity
import com.example.aas_app.data.repository.AppRepository
import com.example.aas_app.util.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun loadQuestionsForPoi(programId: Long, poiId: Long) {
        _questionsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForPoi(programId, poiId)
                _questionsState.postValue(AppState.Success(data))
            } catch (e: RoomSQLException) {
                Log.e("PeclViewModel", "Database error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading questions: ${e.message}", e)
                _questionsState.postValue(AppState.Error(e.message ?: "Error loading questions for POI"))
            }
        }
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            try {
                repository.insertEvaluationResult(result)
                // Optionally post success if a dedicated state is added; for now, no state update as per current design
            } catch (e: RoomSQLException) {
                Log.e("PeclViewModel", "Database error inserting evaluation result: ${e.message}", e)
                // Handle via UI feedback (e.g., pass error to a dedicated insert state if added)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting evaluation result: ${e.message}", e)
                // Handle via UI feedback
            }
        }
    }

    fun getAverageScoreForStudent(studentId: Long, poiId: Long) {
        _averageScoreState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val average = repository.getAverageScoreForStudent(studentId, poiId)
                _averageScoreState.postValue(AppState.Success(average))
            } catch (e: RoomSQLException) {
                Log.e("PeclViewModel", "Database query error calculating average: ${e.message}", e)
                _averageScoreState.postValue(AppState.Error("Database query error: ${e.message ?: "Unknown"}"))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error calculating average score: ${e.message}", e)
                _averageScoreState.postValue(AppState.Error(e.message ?: "Error calculating average score"))
            }
        }
    }

    // Add other aggregate methods as needed, e.g., getCommentsForStudent
}