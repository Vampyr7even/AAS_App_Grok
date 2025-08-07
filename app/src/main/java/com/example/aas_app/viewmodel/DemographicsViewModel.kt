package com.example.aas_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.Result
import com.example.aas_app.data.entity.DemoTemplatesEntity
import com.example.aas_app.data.entity.QuestionRepositoryEntity
import com.example.aas_app.data.entity.ResponseEntity
import com.example.aas_app.data.entity.UserEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DemographicsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _questions = MutableStateFlow<List<QuestionRepositoryEntity>>(emptyList())
    val questions: StateFlow<List<QuestionRepositoryEntity>> = _questions

    private val _demoTemplates = MutableStateFlow<List<DemoTemplatesEntity>>(emptyList())
    val demoTemplates: StateFlow<List<DemoTemplatesEntity>> = _demoTemplates

    private val _selectedQuestions = MutableStateFlow<List<QuestionRepositoryEntity>>(emptyList())
    val selectedQuestions: StateFlow<List<QuestionRepositoryEntity>> = _selectedQuestions

    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users: StateFlow<List<UserEntity>> = _users

    private val _state = MutableLiveData<State<Any>>()
    val state: LiveData<State<Any>> = _state

    init {
        loadQuestions()
        loadTemplates()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            val result = repository.getAllQuestions()
            if (result is Result.Success) {
                _questions.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load questions: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadTemplates() {
        viewModelScope.launch {
            val result = repository.getAllDemoTemplates()
            if (result is Result.Success) {
                _demoTemplates.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load templates: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadQuestionsByIds(ids: List<Int>) {
        viewModelScope.launch {
            val result = repository.getQuestionsByIds(ids)
            if (result is Result.Success) {
                _selectedQuestions.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load selected questions: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadUsersByAssignedProject(project: String) {
        viewModelScope.launch {
            val result = repository.getUsersByAssignedProject(project)
            if (result is Result.Success) {
                _users.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load users: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun insertTemplate(template: DemoTemplatesEntity) {
        viewModelScope.launch {
            val result = repository.insertTemplate(template)
            if (result is Result.Success) {
                loadTemplates()
            } else {
                _state.postValue(State.Error("Failed to insert template: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun updateDemoTemplate(template: DemoTemplatesEntity) {
        viewModelScope.launch {
            val result = repository.updateDemoTemplate(template)
            if (result is Result.Success) {
                loadTemplates()
            } else {
                _state.postValue(State.Error("Failed to update template: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deleteDemoTemplate(template: DemoTemplatesEntity) {
        viewModelScope.launch {
            val result = repository.deleteDemoTemplate(template)
            if (result is Result.Success) {
                loadTemplates()
            } else {
                _state.postValue(State.Error("Failed to delete template: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun insertResponse(response: ResponseEntity) {
        viewModelScope.launch {
            repository.insertResponse(response)
        }
    }

    fun insertQuestion(question: QuestionRepositoryEntity) {
        viewModelScope.launch {
            val result = repository.insertQuestion(question)
            if (result is Result.Success) {
                loadQuestions()
            } else {
                _state.postValue(State.Error("Failed to insert question: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun updateQuestion(question: QuestionRepositoryEntity) {
        viewModelScope.launch {
            val result = repository.updateQuestion(question)
            if (result is Result.Success) {
                loadQuestions()
            } else {
                _state.postValue(State.Error("Failed to update question: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deleteQuestion(question: QuestionRepositoryEntity) {
        viewModelScope.launch {
            val result = repository.deleteQuestion(question)
            if (result is Result.Success) {
                loadQuestions()
            } else {
                _state.postValue(State.Error("Failed to delete question: ${(result as Result.Error).exception.message}"))
            }
        }
    }
}