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

    private val _usersState = MutableLiveData<State<List<UserEntity>>>(State.Success(emptyList()))
    val usersState: LiveData<State<List<UserEntity>>> = _usersState

    private val _instructorsState = MutableLiveData<State<List<UserEntity>>>(State.Success(emptyList()))
    val instructorsState: LiveData<State<List<UserEntity>>> = _instructorsState

    private val _studentsState = MutableLiveData<State<List<PeclStudentEntity>>>(State.Success(emptyList()))
    val studentsState: LiveData<State<List<PeclStudentEntity>>> = _studentsState

    private val _programsState = MutableLiveData<State<List<PeclProgramEntity>>>(State.Success(emptyList()))
    val programsState: LiveData<State<List<PeclProgramEntity>>> = _programsState

    private val _programIdsState = MutableLiveData<State<List<Long>>>(State.Success(emptyList()))
    val programIdsState: LiveData<State<List<Long>>> = _programIdsState

    private val _instructorAssignmentsState = MutableLiveData<State<List<InstructorStudentAssignmentEntity>>>(State.Success(emptyList()))
    val instructorAssignmentsState: LiveData<State<List<InstructorStudentAssignmentEntity>>> = _instructorAssignmentsState

    private val _instructorProgramAssignmentsState = MutableLiveData<State<List<InstructorProgramAssignmentEntity>>>(State.Success(emptyList()))
    val instructorProgramAssignmentsState: LiveData<State<List<InstructorProgramAssignmentEntity>>> = _instructorProgramAssignmentsState

    fun loadUsers() {
        _usersState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllUsers().first()
                _usersState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading users: ${e.message}", e)
                _usersState.postValue(State.Error(e.message ?: "Error loading users"))
            }
        }
    }

    fun loadInstructors() {
        _instructorsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getUsersByRole("instructor").first()
                _instructorsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading instructors: ${e.message}", e)
                _instructorsState.postValue(State.Error(e.message ?: "Error loading instructors"))
            }
        }
    }

    fun loadStudents() {
        _studentsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPeclStudents().first()
                _studentsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading students: ${e.message}", e)
                _studentsState.postValue(State.Error(e.message ?: "Error loading students"))
            }
        }
    }

    fun loadPrograms() {
        _programsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPrograms().first()
                _programsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading programs: ${e.message}", e)
                _programsState.postValue(State.Error(e.message ?: "Error loading programs"))
            }
        }
    }

    fun loadInstructorAssignments(instructorId: Long) {
        _instructorAssignmentsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAssignmentsForInstructor(instructorId).first()
                _instructorAssignmentsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading instructor assignments: ${e.message}", e)
                _instructorAssignmentsState.postValue(State.Error(e.message ?: "Error loading assignments"))
            }
        }
    }

    fun loadProgramIdsForInstructor(instructorId: Long) {
        _programIdsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getProgramIdsForInstructor(instructorId).first()
                _programIdsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading program IDs: ${e.message}", e)
                _programIdsState.postValue(State.Error(e.message ?: "Error loading program IDs"))
            }
        }
    }

    suspend fun insertUserSync(user: UserEntity): Long {
        return try {
            when (val result = repository.insertUser(user)) {
                is AppResult.Success -> result.data
                is AppResult.Error -> {
                    Log.e("DemographicsViewModel", "Error inserting user: ${result.exception.message}", result.exception)
                    -1L
                }
            }
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
                    is AppResult.Error -> _studentsState.postValue(State.Error(result.exception.message ?: "Error inserting student"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error inserting student: ${e.message}", e)
                _studentsState.postValue(State.Error(e.message ?: "Error inserting student"))
            }
        }
    }

    fun updatePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.updatePeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> _studentsState.postValue(State.Error(result.exception.message ?: "Error updating student"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error updating student: ${e.message}", e)
                _studentsState.postValue(State.Error(e.message ?: "Error updating student"))
            }
        }
    }

    fun deletePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deletePeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> _studentsState.postValue(State.Error(result.exception.message ?: "Error deleting student"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting student: ${e.message}", e)
                _studentsState.postValue(State.Error(e.message ?: "Error deleting student"))
            }
        }
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.updateUser(user)) {
                    is AppResult.Success -> loadInstructors()
                    is AppResult.Error -> _instructorsState.postValue(State.Error(result.exception.message ?: "Error updating user"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error updating user: ${e.message}", e)
                _instructorsState.postValue(State.Error(e.message ?: "Error updating user"))
            }
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteUser(user)) {
                    is AppResult.Success -> loadInstructors()
                    is AppResult.Error -> _instructorsState.postValue(State.Error(result.exception.message ?: "Error deleting user"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting user: ${e.message}", e)
                _instructorsState.postValue(State.Error(e.message ?: "Error deleting user"))
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

    fun insertInstructorStudentAssignment(assignment: InstructorStudentAssignmentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertInstructorStudentAssignment(assignment)) {
                    is AppResult.Success -> loadInstructorAssignments(assignment.instructor_id)
                    is AppResult.Error -> _instructorAssignmentsState.postValue(State.Error(result.exception.message ?: "Error inserting assignment"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error inserting assignment: ${e.message}", e)
                _instructorAssignmentsState.postValue(State.Error(e.message ?: "Error inserting assignment"))
            }
        }
    }

    fun deleteAssignmentsForInstructor(instructorId: Long) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteAssignmentsForInstructor(instructorId)) {
                    is AppResult.Success -> loadInstructorAssignments(instructorId)
                    is AppResult.Error -> _instructorAssignmentsState.postValue(State.Error(result.exception.message ?: "Error deleting assignments"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting assignments: ${e.message}", e)
                _instructorAssignmentsState.postValue(State.Error(e.message ?: "Error deleting assignments"))
            }
        }
    }

    fun insertInstructorProgramAssignment(assignment: InstructorProgramAssignmentEntity) {
        viewModelScope.launch {
            try {
                when (val result = repository.insertInstructorProgramAssignment(assignment)) {
                    is AppResult.Success -> loadProgramIdsForInstructor(assignment.instructor_id)
                    is AppResult.Error -> _programIdsState.postValue(State.Error(result.exception.message ?: "Error inserting instructor program assignment"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error inserting instructor program assignment: ${e.message}", e)
                _programIdsState.postValue(State.Error(e.message ?: "Error inserting instructor program assignment"))
            }
        }
    }

    fun deleteInstructorProgramAssignmentsForInstructor(instructorId: Long) {
        viewModelScope.launch {
            try {
                when (val result = repository.deleteInstructorProgramAssignmentsForInstructor(instructorId)) {
                    is AppResult.Success -> loadProgramIdsForInstructor(instructorId)
                    is AppResult.Error -> _programIdsState.postValue(State.Error(result.exception.message ?: "Error deleting instructor program assignments"))
                }
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting instructor program assignments: ${e.message}", e)
                _programIdsState.postValue(State.Error(e.message ?: "Error deleting instructor program assignments"))
            }
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