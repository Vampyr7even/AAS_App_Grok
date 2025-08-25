package com.example.aas_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemographicsViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> get() = _users

    private val _instructors = MutableLiveData<List<UserEntity>>()
    val instructors: LiveData<List<UserEntity>> get() = _instructors

    private val _students = MutableLiveData<List<UserEntity>>()
    val students: LiveData<List<UserEntity>> get() = _students

    private val _programs = MutableLiveData<List<PeclProgramEntity>>()
    val programs: LiveData<List<PeclProgramEntity>> get() = _programs

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val data = repository.getAllUsers().first()
                _users.value = data
            } catch (e: Exception) {
                _users.value = emptyList()
            }
        }
    }

    fun loadInstructors() {
        viewModelScope.launch {
            try {
                val data = repository.getUsersByRole("instructor").first()
                _instructors.value = data
            } catch (e: Exception) {
                _instructors.value = emptyList()
            }
        }
    }

    fun loadStudents() {
        viewModelScope.launch {
            try {
                val data = repository.getUsersByRole("student").first()
                _students.value = data
            } catch (e: Exception) {
                _students.value = emptyList()
            }
        }
    }

    fun loadPrograms() {
        viewModelScope.launch {
            try {
                val data = repository.getAllPrograms().first()
                _programs.value = data
            } catch (e: Exception) {
                _programs.value = emptyList()
            }
        }
    }

    fun insertUser(user: UserEntity) {
        viewModelScope.launch {
            repository.insertUser(user)
            loadUsers()
        }
    }

    suspend fun insertUserSync(user: UserEntity): Long {
        return repository.insertUser(user)
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            loadUsers()
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            repository.deleteUser(user)
            loadUsers()
        }
    }

    fun insertAssignment(assignment: InstructorStudentAssignmentEntity) {
        viewModelScope.launch {
            repository.insertAssignment(assignment)
        }
    }

    fun deleteAssignmentsForInstructor(instructorId: Long) {
        viewModelScope.launch {
            repository.deleteAssignmentsForInstructor(instructorId)
        }
    }

    fun getAssignmentsForInstructor(instructorId: Long): LiveData<List<InstructorStudentAssignmentEntity>> {
        return repository.getAssignmentsForInstructor(instructorId).asLiveData()
    }
}