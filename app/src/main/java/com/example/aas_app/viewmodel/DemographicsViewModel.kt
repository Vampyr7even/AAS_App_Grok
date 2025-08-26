package com.example.aas_app.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.AppRepository
import com.example.aas_app.data.entity.InstructorStudentAssignmentEntity
import com.example.aas_app.data.entity.InstructorProgramAssignmentEntity
import com.example.aas_app.data.entity.PeclProgramEntity
import com.example.aas_app.data.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DemographicsViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    private val _users = MutableLiveData<List<UserEntity>>()
    val users: LiveData<List<UserEntity>> = _users

    private val _instructors = MutableLiveData<List<UserEntity>>()
    val instructors: LiveData<List<UserEntity>> = _instructors

    private val _instructorsWithPrograms = MutableLiveData<List<InstructorWithProgram>>()
    val instructorsWithPrograms: LiveData<List<InstructorWithProgram>> = _instructorsWithPrograms

    private val _students = MutableLiveData<List<UserEntity>>()
    val students: LiveData<List<UserEntity>> = _students

    private val _programs = MutableLiveData<List<PeclProgramEntity>>()
    val programs: LiveData<List<PeclProgramEntity>> = _programs

    private val _assignments = MutableLiveData<List<InstructorStudentAssignmentEntity>>()
    val assignments: LiveData<List<InstructorStudentAssignmentEntity>> = _assignments

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
                _instructors.postValue(instructorList)
                // Load instructors with programs
                val instructorsWithPrograms = instructorList.map { instructor ->
                    val programNames = repository.getProgramsForInstructor(instructor.id).first().joinToString(", ")
                    InstructorWithProgram(instructor, if (programNames.isEmpty()) null else programNames)
                }
                _instructorsWithPrograms.postValue(instructorsWithPrograms)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading instructors: ${e.message}", e)
            }
        }
    }

    fun loadStudents() {
        viewModelScope.launch {
            try {
                val studentList = repository.getUsersByRole("student").first()
                _students.postValue(studentList)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading students: ${e.message}", e)
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
                } else if (user.role == "student") {
                    loadStudents()
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
                } else if (user.role == "student") {
                    loadStudents()
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
                } else if (user.role == "student") {
                    loadStudents()
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
                loadInstructors() // Refresh to update program assignments
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
        viewModelScope.launch {
            try {
                val assignmentList = repository.getAssignmentsForInstructor(instructorId).first()
                _assignments.postValue(assignmentList)
            } catch (e: Exception) {
                Log.e("DemographicsViewModel", "Error loading assignments: ${e.message}", e)
            }
        }
        return _assignments
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

    fun canDeleteInstructor(instructorId: Long): Flow<Boolean> {
        val studentAssignments = repository.getAssignmentsForInstructor(instructorId).map { it.size == 0 }
        val programAssignments = repository.getProgramsForInstructor(instructorId).map { it.size == 0 }
        return combine(studentAssignments, programAssignments) { noStudents, noPrograms ->
            noStudents && noPrograms
        }
    }
}

data class InstructorWithProgram(
    val instructor: UserEntity,
    val programName: String?
)