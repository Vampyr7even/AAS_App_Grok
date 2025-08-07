package com.example.aas_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.AppResult
import com.example.aas_app.data.UserEntity
import kotlinx.coroutines.launch

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
            val result = repository.getAllUsers()
            when (result) {
                is AppResult.Success -> _users.value = result.data
                is AppResult.Error -> { /* Handle error */ }
            }
        }
    }

    fun loadInstructors() {
        viewModelScope.launch {
            val result = repository.getUsersByRole("instructor")
            when (result) {
                is AppResult.Success -> _instructors.value = result.data
                is AppResult.Error -> { /* Handle error */ }
            }
        }
    }

    fun loadStudents() {
        viewModelScope.launch {
            val result = repository.getUsersByRole("student")
            when (result) {
                is AppResult.Success -> _students.value = result.data
                is AppResult.Error -> { /* Handle error */ }
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