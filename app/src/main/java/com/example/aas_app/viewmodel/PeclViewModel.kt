package com.example.aas_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.entities.PeclEvaluationResultEntity
import com.example.aas_app.data.entities.PeclQuestionEntity
import com.example.aas_app.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class PeclViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    val questionsState: LiveData<AppState<List<PeclQuestionEntity>>> = MutableLiveData()
    val evaluationResultsState: LiveData<AppState<List<PeclEvaluationResultEntity>>> = MutableLiveData()

    fun loadQuestionsForPoi(program: String, poi: String) {
        (questionsState as MutableLiveData).value = AppState.Loading()
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForPoi(program, poi) // Assume DAO method updated for JOIN
                (questionsState as MutableLiveData).postValue(AppState.Success(data))
            } catch (e: Exception) {
                (questionsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error loading questions for POI"))
            }
        }
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            try {
                repository.insertEvaluationResult(result)
                // Reload if needed
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    // Add other methods as needed for average scores, etc.
}