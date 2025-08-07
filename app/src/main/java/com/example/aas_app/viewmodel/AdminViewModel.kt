package com.example.aas_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.Result
import com.example.aas_app.data.entity.PeclTaskEntity
import com.example.aas_app.data.entity.ProjectEntity
import com.example.aas_app.data.entity.UserEntity
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(private val repository: AppRepository) : ViewModel() {

    private val _projects = MutableStateFlow<List<ProjectEntity>>(emptyList())
    val projects: StateFlow<List<ProjectEntity>> = _projects

    private val _users = MutableStateFlow<List<UserEntity>>(emptyList())
    val users: StateFlow<List<UserEntity>> = _users

    private val _peclTasks = MutableStateFlow<List<PeclTaskEntity>>(emptyList())
    val peclTasks: StateFlow<List<PeclTaskEntity>> = _peclTasks

    private val _state = MutableLiveData<State<Any>>()
    val state: LiveData<State<Any>> = _state

    init {
        loadProjects()
        loadUsers()
        loadPeclTasks()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _state.postValue(State.Loading)
            val result = repository.getAllProjects()
            if (result is Result.Success) {
                _projects.value = result.data
                if (_projects.value.isEmpty()) {
                    _state.postValue(State.Error("No projects found"))
                } else {
                    _state.postValue(State.Success(result.data))
                }
            } else {
                _state.postValue(State.Error("Failed to load projects: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            val result = repository.getAllUsers()
            if (result is Result.Success) {
                _users.value = result.data
            } else {
                _state.postValue(State.Error("Failed to load users: ${(result as Result.Error).exception.message}"))
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

    fun insertProject(project: ProjectEntity) {
        viewModelScope.launch {
            val result = repository.insertProject(project)
            if (result is Result.Success) {
                loadProjects()
            } else {
                _state.postValue(State.Error("Insert failed: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch {
            val result = repository.updateProject(project)
            if (result is Result.Success) {
                loadProjects()
            } else {
                _state.postValue(State.Error("Update failed: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            val result = repository.deleteProject(project)
            if (result is Result.Success) {
                loadProjects()
            } else {
                _state.postValue(State.Error("Delete failed: referenced? ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun insertUser(user: UserEntity) {
        viewModelScope.launch {
            val result = repository.insertUser(user)
            if (result is Result.Success) {
                loadUsers()
            } else {
                _state.postValue(State.Error("Insert failed: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            val result = repository.updateUser(user)
            if (result is Result.Success) {
                loadUsers()
            } else {
                _state.postValue(State.Error("Update failed: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            val result = repository.deleteUser(user)
            if (result is Result.Success) {
                loadUsers()
            } else {
                _state.postValue(State.Error("Delete failed: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    fun deletePeclTask(task: PeclTaskEntity) {
        viewModelScope.launch {
            val result = repository.deletePeclTask(task)
            if (result is Result.Success) {
                loadPeclTasks()
            } else {
                _state.postValue(State.Error("Delete failed: ${(result as Result.Error).exception.message}"))
            }
        }
    }

    // Add similar for other entities like scales, etc.
}