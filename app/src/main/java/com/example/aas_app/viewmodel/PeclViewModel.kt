package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.entity.PeclEvaluationResultEntity
import com.example.aas_app.data.entity.PeclQuestionEntity
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

    // Add other aggregate methods as needed, e.g., getCommentsForStudent
}