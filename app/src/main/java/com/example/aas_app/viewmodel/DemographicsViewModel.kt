package com.example.aas_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.AppResult
import com.example.aas_app.data.entity.UserEntity
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class DemographicsViewModel(private val repository: AppRepository) : ViewModel() {

    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> get() = _users

    private val _instructors = MutableLiveData<List<UserEntity>>()
    val instructors: LiveData<List<UserEntity>> get() = _instructors

    private val _students = MutableLiveData<List<UserEntity>>()
    val students: LiveData<List<UserEntity>> get() = _students

    // Load methods
    fun loadUsers() {
        viewModelScope.launch {
            try {
                _users.value = repository.getAllUsers().first()
            } catch (e: Exception) {
                // Handle error, e.g., log or post empty list
                _users.value = emptyList()
            }
        }
    }

    fun loadInstructors() {
        viewModelScope.launch {
            try {
                _instructors.value = repository.getUsersByRole("instructor").first()
            } catch (e: Exception) {
                _instructors.value = emptyList()
            }
        }
    }

    fun loadStudents() {
        viewModelScope.launch {
            try {
                _students.value = repository.getUsersByRole("student").first()
            } catch (e: Exception) {
                _students.value = emptyList()
            }
        }
    }

    // CRUD for users
    fun insertUser(user: UserEntity) {
        viewModelScope.launch {
            val result = repository.insertUser(user)
            when (result) {
                is AppResult.Success -> loadUsers()
                is AppResult.Error -> { /* Handle error */ }
            }
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            val result = repository.updateUser(user)
            when (result) {
                is AppResult.Success -> loadUsers()
                is AppResult.Error -> { /* Handle error */ }
            }
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            val result = repository.deleteUser(user)
            when (result) {
                is AppResult.Success -> loadUsers()
                is AppResult.Error -> { /* Handle error */ }
            }
        }
    }
}