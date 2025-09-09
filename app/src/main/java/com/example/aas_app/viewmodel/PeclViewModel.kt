package com.example.aas_app.viewmodel

import android.util.Log
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
class PeclViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    val programsState = MutableLiveData<State<List<PeclProgramEntity>>>(State.Success(emptyList()))
    val studentsState = MutableLiveData<State<List<PeclStudentEntity>>>(State.Success(emptyList()))
    val evaluationResultsState = MutableLiveData<State<List<PeclEvaluationResultEntity>>>(State.Success(emptyList()))
    val commentsState = MutableLiveData<State<List<CommentEntity>>>(State.Success(emptyList()))
    val studentsForInstructorAndProgramState = MutableLiveData<State<List<PeclStudentEntity>>>(State.Success(emptyList()))
    val evaluationsForStudentAndTaskState = MutableLiveData<State<List<PeclEvaluationResultEntity>>>(State.Success(emptyList()))
    val questionsState = MutableLiveData<State<List<QuestionWithTask>>>(State.Success(emptyList()))
    val tasksState = MutableLiveData<State<List<PeclTaskEntity>>>(State.Success(emptyList()))
    val poisState = MutableLiveData<State<List<PeclPoiEntity>>>(State.Success(emptyList()))

    fun loadProgramsForInstructor(instructorId: Long) {
        programsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getProgramsForInstructor(instructorId).first()
                programsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading programs: ${e.message}", e)
                programsState.postValue(State.Error(e.message ?: "Error loading programs"))
            }
        }
    }

    fun loadPoisForProgram(programId: Long) {
        poisState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getPoisForProgram(programId).first()
                poisState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading POIs for program: ${e.message}", e)
                poisState.postValue(State.Error(e.message ?: "Error loading POIs"))
            }
        }
    }

    fun loadStudentsForInstructorAndProgram(instructorId: Long, programId: Long) {
        studentsForInstructorAndProgramState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForInstructorAndProgram(instructorId, programId).first()
                studentsForInstructorAndProgramState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading students for instructor and program: ${e.message}", e)
                studentsForInstructorAndProgramState.postValue(State.Error(e.message ?: "Error loading students"))
            }
        }
    }

    fun loadStudentsForInstructor(instructorId: Long) {
        studentsForInstructorAndProgramState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForInstructorAndProgram(instructorId, 0L).first()
                studentsForInstructorAndProgramState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading students for instructor: ${e.message}", e)
                studentsForInstructorAndProgramState.postValue(State.Error(e.message ?: "Error loading students"))
            }
        }
    }

    fun loadStudents() {
        studentsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllPeclStudents().first()
                studentsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading students: ${e.message}", e)
                studentsState.postValue(State.Error(e.message ?: "Error loading students"))
            }
        }
    }

    fun loadEvaluationResultsForStudent(studentId: Long) {
        evaluationResultsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getEvaluationResultsForStudent(studentId).first()
                evaluationResultsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading evaluation results: ${e.message}", e)
                evaluationResultsState.postValue(State.Error(e.message ?: "Error loading evaluation results"))
            }
        }
    }

    fun loadCommentsForStudent(studentId: Long) {
        commentsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getCommentsForStudent(studentId).first()
                commentsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading comments: ${e.message}", e)
                commentsState.postValue(State.Error(e.message ?: "Error loading comments"))
            }
        }
    }

    fun getEvaluationsForStudent(studentId: Long): Flow<List<PeclEvaluationResultEntity>> {
        return repository.getEvaluationResultsForStudent(studentId)
    }

    fun getProgramById(programId: Long, onResult: (PeclProgramEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val program = repository.getProgramById(programId)
                onResult(program)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting program by ID: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getInstructorById(instructorId: Long, onResult: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val instructor = repository.getUserById(instructorId).first()
                onResult(instructor)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting instructor by ID: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getTaskById(taskId: Long, onResult: (PeclTaskEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val task = repository.getTaskById(taskId)
                onResult(task)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting task by ID: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getPeclStudentById(studentId: Long, onResult: (PeclStudentEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val student = repository.getPeclStudentById(studentId).first()
                onResult(student)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting student by ID: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun getScaleByName(name: String, onResult: (ScaleEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val scale = repository.getScaleByName(name)
                onResult(scale)
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting scale by name: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun loadQuestionsForTask(taskId: Long) {
        questionsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data: List<QuestionWithTask> = repository.getQuestionsForTask(taskId).first()
                questionsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading questions for task: ${e.message}", e)
                questionsState.postValue(State.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun loadQuestionsForPoi(poiId: Long) {
        questionsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data: List<QuestionWithTask> = repository.getQuestionsForPoi(poiId).first()
                questionsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading questions for POI: ${e.message}", e)
                questionsState.postValue(State.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun loadEvaluationsForStudentAndTask(studentId: Long, taskId: Long) {
        evaluationsForStudentAndTaskState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getEvaluationsForStudentAndTask(studentId, taskId).first()
                evaluationsForStudentAndTaskState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading evaluations for student and task: ${e.message}", e)
                evaluationsForStudentAndTaskState.postValue(State.Error(e.message ?: "Error loading evaluations"))
            }
        }
    }

    fun getTaskGradeForStudent(studentId: Long, taskId: Long): Flow<Double?> {
        return repository.getTaskGradeForStudent(studentId, taskId)
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.insertEvaluationResult(result)) {
                    is AppResult.Success -> loadEvaluationResultsForStudent(result.student_id)
                    is AppResult.Error -> evaluationResultsState.postValue(State.Error(appResult.exception.message ?: "Error inserting evaluation result"))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting evaluation result: ${e.message}", e)
                evaluationResultsState.postValue(State.Error(e.message ?: "Error inserting evaluation result"))
            }
        }
    }

    fun deleteEvaluationsForStudentAndTask(studentId: Long, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.deleteEvaluationsForStudentAndTask(studentId, taskId)) {
                    is AppResult.Success -> loadEvaluationsForStudentAndTask(studentId, taskId)
                    is AppResult.Error -> evaluationsForStudentAndTaskState.postValue(State.Error(appResult.exception.message ?: "Error deleting evaluations"))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error deleting evaluations: ${e.message}", e)
                evaluationsForStudentAndTaskState.postValue(State.Error(e.message ?: "Error deleting evaluations"))
            }
        }
    }

    fun insertPeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.insertPeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> studentsState.postValue(State.Error(appResult.exception.message ?: "Error inserting student"))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting student: ${e.message}", e)
                studentsState.postValue(State.Error(e.message ?: "Error inserting student"))
            }
        }
    }

    fun updatePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.updatePeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> studentsState.postValue(State.Error(appResult.exception.message ?: "Error updating student"))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error updating student: ${e.message}", e)
                studentsState.postValue(State.Error(e.message ?: "Error updating student"))
            }
        }
    }

    fun deletePeclStudent(student: PeclStudentEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.deletePeclStudent(student)) {
                    is AppResult.Success -> loadStudents()
                    is AppResult.Error -> studentsState.postValue(State.Error(appResult.exception.message ?: "Error deleting student"))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error deleting student: ${e.message}", e)
                studentsState.postValue(State.Error(e.message ?: "Error deleting student"))
            }
        }
    }

    fun loadAllTasks() {
        tasksState.value = State.Loading
        viewModelScope.launch {
            try {
                val data = repository.getAllTasks().first()
                tasksState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading all tasks: ${e.message}", e)
                tasksState.postValue(State.Error(e.message ?: "Error loading tasks"))
            }
        }
    }

    fun getTaskForQuestion(questionId: Long, onResult: (PeclTaskEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val taskId = repository.getTaskIdForQuestion(questionId)
                if (taskId != null) {
                    val task = repository.getTaskById(taskId)
                    onResult(task)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error getting task for question: ${e.message}", e)
                onResult(null)
            }
        }
    }

    fun insertQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.insertQuestion(question, taskId)) {
                    is AppResult.Success -> loadAllQuestions()
                    is AppResult.Error -> questionsState.postValue(State.Error(appResult.exception.message ?: "Error inserting question"))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error inserting question: ${e.message}", e)
                questionsState.postValue(State.Error(e.message ?: "Error inserting question"))
            }
        }
    }

    fun updateQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.updateQuestion(question, taskId)) {
                    is AppResult.Success -> loadAllQuestions()
                    is AppResult.Error -> questionsState.postValue(State.Error(appResult.exception.message ?: "Error updating question"))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error updating question: ${e.message}", e)
                questionsState.postValue(State.Error(e.message ?: "Error updating question"))
            }
        }
    }

    fun deleteQuestion(question: PeclQuestionEntity) {
        viewModelScope.launch {
            try {
                when (val appResult = repository.deleteQuestion(question)) {
                    is AppResult.Success -> loadAllQuestions()
                    is AppResult.Error -> questionsState.postValue(State.Error(appResult.exception.message ?: "Error deleting question"))
                }
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error deleting question: ${e.message}", e)
                questionsState.postValue(State.Error(e.message ?: "Error deleting question"))
            }
        }
    }

    fun loadAllQuestions() {
        questionsState.value = State.Loading
        viewModelScope.launch {
            try {
                val data: List<QuestionWithTask> = repository.getAllQuestionsWithTasks().first()
                questionsState.postValue(State.Success(data))
            } catch (e: Exception) {
                Log.e("PeclViewModel", "Error loading all questions: ${e.message}", e)
                questionsState.postValue(State.Error(e.message ?: "Error loading questions"))
            }
        }
    }
}