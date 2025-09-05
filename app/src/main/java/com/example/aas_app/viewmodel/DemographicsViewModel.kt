package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.entity.InstructorProgramAssignmentEntity
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.InstructorWithProgram
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemographicsViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> = _users

    private val _instructorsWithPrograms = MutableLiveData<List<InstructorWithProgram>>()
    val instructorsWithPrograms: LiveData<List<InstructorWithProgram>> = _instructorsWithPrograms

    private val _programs = MutableLiveData<List<PeclProgramEntity>>()
    val programs: LiveData<List<PeclProgramEntity>> = _programs

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val userList = repository.getAllUsers().first()
                _users.postValue(userList)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading users: ${e.message}", e)
            }
        }
    }

    fun loadInstructors() {
        viewModelScope.launch {
            try {
                val instructorList = repository.getUsersByRole("instructor").first()
                val instructorsWithPrograms = instructorList.map { instructor ->
                    val program = repository.getProgramsForInstructor(instructor.id).first().firstOrNull()?.name
                    InstructorWithProgram(instructor, program)
                }.sortedBy { it.instructor.fullName }
                _instructorsWithPrograms.postValue(instructorsWithPrograms)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading instructors: ${e.message}", e)
            }
        }
    }

    fun loadPrograms() {
        viewModelScope.launch {
            try {
                val programList = repository.getAllPrograms().first()
                _programs.postValue(programList)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading programs: ${e.message}", e)
            }
        }
    }

    fun insertUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.insertUser(user)
                if (user.role == "instructor") {
                    loadInstructors()
                }
                loadUsers()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error inserting user: ${e.message}", e)
            }
        }
    }

    suspend fun insertUserSync(user: UserEntity): Long = repository.insertUser(user)

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.updateUser(user)
                if (user.role == "instructor") {
                    loadInstructors()
                }
                loadUsers()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error updating user: ${e.message}", e)
            }
        }
    }

    fun deleteUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.deleteUser(user)
                if (user.role == "instructor") {
                    loadInstructors()
                }
                loadUsers()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting user: ${e.message}", e)
            }
        }
    }

    fun insertAssignment(assignment: InstructorStudentAssignmentEntity) {
        viewModelScope.launch {
            try {
                repository.insertAssignment(assignment)
                loadInstructors()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error inserting assignment: ${e.message}", e)
            }
        }
    }

    fun deleteAssignmentsForInstructor(instructorId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteAssignmentsForInstructor(instructorId)
                loadInstructors()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting assignments: ${e.message}", e)
            }
        }
    }

    fun getAssignmentsForInstructor(instructorId: Long): LiveData<List<InstructorStudentAssignmentEntity>> {
        val assignmentsLiveData = MutableLiveData<List<InstructorStudentAssignmentEntity>>()
        viewModelScope.launch {
            try {
                val assignmentList = repository.getAssignmentsForInstructor(instructorId).first()
                assignmentsLiveData.postValue(assignmentList)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading assignments: ${e.message}", e)
            }
        }
        return assignmentsLiveData
    }

    suspend fun getAssignmentsForInstructorSync(instructorId: Long): List<InstructorStudentAssignmentEntity> {
        return try {
            repository.getAssignmentsForInstructor(instructorId).first()
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error loading assignments sync: ${e.message}", e)
            emptyList()
        }
    }

    fun insertInstructorProgramAssignment(assignment: InstructorProgramAssignmentEntity) {
        viewModelScope.launch {
            try {
                repository.insertInstructorProgramAssignment(assignment)
                loadInstructors()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error inserting instructor program assignment: ${e.message}", e)
            }
        }
    }

    fun deleteInstructorProgramAssignmentsForInstructor(instructorId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteInstructorProgramAssignmentsForInstructor(instructorId)
                loadInstructors()
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error deleting instructor program assignments: ${e.message}", e)
            }
        }
    }

    fun getProgramsForInstructor(instructorId: Long): LiveData<List<PeclProgramEntity>> {
        val programsLiveData = MutableLiveData<List<PeclProgramEntity>>()
        viewModelScope.launch {
            try {
                val programList = repository.getProgramsForInstructor(instructorId).first()
                programsLiveData.postValue(programList)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading programs for instructor: ${e.message}", e)
            }
        }
        return programsLiveData
    }

    suspend fun canDeleteInstructor(instructorId: Long): Boolean {
        return try {
            val studentAssignments = repository.getAssignmentsForInstructor(instructorId).first()
            val programAssignments = repository.getProgramsForInstructor(instructorId).first()
            studentAssignments.isEmpty() && programAssignments.isEmpty()
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error checking if instructor can be deleted: ${e.message}", e)
            false
        }
    }

    suspend fun getAssignmentForStudent(studentId: Long): InstructorStudentAssignmentEntity? {
        return try {
            repository.getAssignmentsForStudent(studentId).first().firstOrNull()
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error getting assignment for student: ${e.message}", e)
            null
        }
    }

    fun getInstructorName(instructorId: Long): String? {
        var name: String? = null
        viewModelScope.launch {
            try {
                name = repository.getInstructorName(instructorId)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error getting instructor name: ${e.message}", e)
            }
        }
        return name
    }

    suspend fun getProgramIdsForInstructorSync(instructorId: Long): List<Long> {
        return try {
            repository.getProgramIdsForInstructor(instructorId).first()
        } catch (e: Exception) {
            Log.e("DemographicsViewModel", "Error loading program IDs sync: ${e.message}", e)
            emptyList()
        }
    }
}