package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.AppResult
import com.example.aas_app.data.entity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemographicsViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _usersState = MutableLiveData<AppState<List<UserEntity>>>()
    val usersState: LiveData<AppState<List<UserEntity>>> = _usersState

    private val _instructorsState = MutableLiveData<AppState<List<UserEntity>>>()
    val instructorsState: LiveData<AppState<List<UserEntity>>> = _instructorsState

    private val _studentsState = MutableLiveData<AppState<List<PeclStudentEntity>>>()
    val studentsState: LiveData<AppState<List<PeclStudentEntity>>> = _studentsState

    private val _programsState = MutableLiveData<AppState<List<PeclProgramEntity>>>()
    val programsState: LiveData<AppState<List<PeclProgramEntity>>> = _programsState

    private val _programIdsState = MutableLiveData<AppState<List<Long>>>()
    val programIdsState: LiveData<AppState<List<Long>>> = _programIdsState

    private val _instructorAssignmentsState = MutableLiveData<AppState<List<InstructorStudentAssignmentEntity>>>()
    val instructorAssignmentsState: LiveData<AppState<List<InstructorStudentAssignmentEntity>>> = _instructorAssignmentsState

    private val _instructorProgramAssignmentsState = MutableLiveData<AppState<List<InstructorProgramAssignmentEntity>>>()
    val instructorProgramAssignmentsState: LiveData<AppState<List<InstructorProgramAssignmentEntity>>> = _instructorProgramAssignmentsState

    fun loadUsers() {
        _usersState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllUsers().first()
                _usersState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading users: ${e.message}", e)
                _usersState.postValue(AppState.Error(e.message ?: "Error loading users"))
            }
        }
    }

    fun loadInstructors() {
        _instructorsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getUsersByRole("instructor").first()
                _instructorsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading instructors: ${e.message}", e)
                _instructorsState.postValue(AppState.Error(e.message ?: "Error loading instructors"))
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
                Log.e("DemographicsViewModel", "Error loading students: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error loading students"))
            }
        }
    }

    fun loadPrograms() {
        _programsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPrograms().first()
                _programsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading programs: ${e.message}", e)
                _programsState.postValue(AppState.Error(e.message ?: "Error loading programs"))
            }
        }
    }

    fun loadInstructorAssignments(instructorId: Long) {
        _instructorAssignmentsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAssignmentsForInstructor(instructorId).first()
                _instructorAssignmentsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading instructor assignments: ${e.message}", e)
                _instructorAssignmentsState.postValue(AppState.Error(e.message ?: "Error loading assignments"))
            }
        }
    }

    fun loadProgramIdsForInstructor(instructorId: Long) {
        _programIdsState.value = AppState.Loading
        viewModelScope.launch {
            try {
                val data = repository.getProgramIdsForInstructor(instructorId).first()
                _programIdsState.postValue(AppState.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading program IDs: ${e.message}", e)
                _programIdsState.postValue(AppState.Error(e.message ?: "Error loading program IDs"))
            }
        }
    }

    suspend fun insertUserSync(user: UserEntity): Long {
        return try {
            repository.insertUser(user)
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error inserting user: ${e.message}", e)
            -1L
        }
    }

    fun insertPeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertPeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> _studentsState.postValue(AppState.Error(result.exception.message ?: "Error inserting student"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error inserting student: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error inserting student"))
            }
        }
    }

    fun updatePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.updatePeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> _studentsState.postValue(AppState.Error(result.exception.message ?: "Error updating student"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error updating student: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error updating student"))
            }
        }
    }

    fun deletePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deletePeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> _studentsState.postValue(AppState.Error(result.exception.message ?: "Error deleting student"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting student: ${e.message}", e)
                _studentsState.postValue(AppState.Error(e.message ?: "Error deleting student"))
            }
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.updateUser(user)
                loadInstructors()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error updating user: ${e.message}", e)
                _instructorsState.postValue(AppState.Error(e.message ?: "Error updating user"))
            }
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.deleteUser(user)
                loadInstructors()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting user: ${e.message}", e)
                _instructorsState.postValue(AppState.Error(e.message ?: "Error deleting user"))
            }
        }
    }

    fun getAssignmentsForInstructor(instructorId: Long): Flow<List<InstructorStudentAssignmentEntity>> {
        return repository.getAssignmentsForInstructor(instructorId)
    }

    suspend fun getAssignmentsForInstructorSync(instructorId: Long): List<InstructorStudentAssignmentEntity> {
        return try {
            repository.getAssignmentsForInstructor(instructorId).first()
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error getting assignments: ${e.message}", e)
            emptyList()
        }
    }

    fun getProgramIdsForInstructor(instructorId: Long): Flow<List<Long>> {
        return repository.getProgramIdsForInstructor(instructorId)
    }

    suspend fun getProgramIdsForInstructorSync(instructorId: Long): List<Long> {
        return try {
            repository.getProgramIdsForInstructor(instructorId).first()
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error getting program IDs: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun insertAssignment(assignment: InstructorStudentAssignmentEntity) {
        try {
            repository.insertAssignment(assignment)
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error inserting assignment: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteAssignmentsForInstructor(instructorId: Long) {
        try {
            repository.deleteAssignmentsForInstructor(instructorId)
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error deleting assignments: ${e.message}", e)
            throw e
        }
    }

    suspend fun insertInstructorProgramAssignment(assignment: InstructorProgramAssignmentEntity) {
        try {
            repository.insertInstructorProgramAssignment(assignment)
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error inserting instructor program assignment: ${e.message}", e)
            throw e
        }
    }

    suspend fun deleteInstructorProgramAssignmentsForInstructor(instructorId: Long) {
        try {
            repository.deleteInstructorProgramAssignmentsForInstructor(instructorId)
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error deleting instructor program assignments: ${e.message}", e)
            throw e
        }
    }

    suspend fun canDeleteInstructor(instructorId: Long): Boolean {
        return try {
            val assignments = getAssignmentsForInstructorSync(instructorId)
            assignments.isEmpty()
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error checking if instructor can be deleted: ${e.message}", e)
            false
        }
    }
}