package com.example.aas_app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aas_app.data.entities.PeclEvaluationResultEntity
import com.example.aas_app.data.entities.PeclPoiEntity
import com.example.aas_app.data.entities.PeclProgramEntity
import com.example.aas_app.data.entities.PeclQuestionEntity
import com.example.aas_app.data.entities.PeclScaleEntity
import com.example.aas_app.data.entities.PeclTaskEntity
import com.example.aas_app.data.entities.UserEntity
import com.example.aas_app.data.repository.AppRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

sealed class AppState<T> {
    class Loading<T> : AppState<T>()
    data class Success<T>(val data: T) : AppState<T>()
    data class Error<T>(val message: String) : AppState<T>()
}

@HiltViewModel
class AdminViewModel @Inject constructor(private val repository: AppRepository) : ViewModel() {

    val programsState: LiveData<AppState<List<PeclProgramEntity>>> = MutableLiveData()
    val poisState: LiveData<AppState<List<PeclPoiEntity>>> = MutableLiveData()
    val tasksState: LiveData<AppState<List<PeclTaskEntity>>> = MutableLiveData()
    val questionsState: LiveData<AppState<List<PeclQuestionEntity>>> = MutableLiveData()
    val scalesState: LiveData<AppState<List<PeclScaleEntity>>> = MutableLiveData()
    val studentsState: LiveData<AppState<List<UserEntity>>> = MutableLiveData()

    fun loadPrograms() {
        (programsState as MutableLiveData).value = AppState.Loading()
        viewModelScope.launch {
            try {
                val data = repository.getAllPrograms()
                (programsState as MutableLiveData).postValue(AppState.Success(data))
            } catch (e: Exception) {
                (programsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error loading programs"))
            }
        }
    }

    fun loadPoisForProgram(programId: Long) {
        (poisState as MutableLiveData).value = AppState.Loading()
        viewModelScope.launch {
            try {
                val data = repository.getPoisForProgram(programId)
                (poisState as MutableLiveData).postValue(AppState.Success(data))
            } catch (e: Exception) {
                (poisState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error loading POIs"))
            }
        }
    }

    fun loadTasksForPoi(poiId: Long) {
        (tasksState as MutableLiveData).value = AppState.Loading()
        viewModelScope.launch {
            try {
                val data = repository.getTasksForPoi(poiId)
                (tasksState as MutableLiveData).postValue(AppState.Success(data))
            } catch (e: Exception) {
                (tasksState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error loading tasks"))
            }
        }
    }

    fun loadQuestionsForTask(taskId: Long) {
        (questionsState as MutableLiveData).value = AppState.Loading()
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsForTask(taskId)
                (questionsState as MutableLiveData).postValue(AppState.Success(data))
            } catch (e: Exception) {
                (questionsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error loading questions"))
            }
        }
    }

    fun loadScales() {
        (scalesState as MutableLiveData).value = AppState.Loading()
        viewModelScope.launch {
            try {
                val data = repository.getAllScales()
                (scalesState as MutableLiveData).postValue(AppState.Success(data))
            } catch (e: Exception) {
                (scalesState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error loading scales"))
            }
        }
    }

    fun loadQuestionsByIds(ids: List<Long>) {
        (questionsState as MutableLiveData).value = AppState.Loading()
        viewModelScope.launch {
            try {
                val data = repository.getQuestionsByIds(ids)
                (questionsState as MutableLiveData).postValue(AppState.Success(data))
            } catch (e: Exception) {
                (questionsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error loading questions by IDs"))
            }
        }
    }

    fun loadStudentsForProgram(programId: Long) {
        (studentsState as MutableLiveData).value = AppState.Loading()
        viewModelScope.launch {
            try {
                val data = repository.getStudentsForProgram(programId)
                (studentsState as MutableLiveData).postValue(AppState.Success(data))
            } catch (e: Exception) {
                (studentsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error loading students for program"))
            }
        }
    }

    fun insertProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                repository.insertProgram(program)
                loadPrograms()
            } catch (e: Exception) {
                (programsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error inserting program"))
            }
        }
    }

    fun updateProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                repository.updateProgram(program)
                loadPrograms()
            } catch (e: Exception) {
                (programsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error updating program"))
            }
        }
    }

    fun deleteProgram(program: PeclProgramEntity) {
        viewModelScope.launch {
            try {
                repository.deleteProgram(program)
                loadPrograms()
            } catch (e: Exception) {
                (programsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Cannot delete program - referenced elsewhere"))
            }
        }
    }

    fun insertPoi(poi: PeclPoiEntity) {
        viewModelScope.launch {
            try {
                repository.insertPoi(poi)
                loadPoisForProgram(poi.programId)
            } catch (e: Exception) {
                (poisState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error inserting POI"))
            }
        }
    }

    fun updatePoi(poi: PeclPoiEntity) {
        viewModelScope.launch {
            try {
                repository.updatePoi(poi)
                loadPoisForProgram(poi.programId)
            } catch (e: Exception) {
                (poisState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error updating POI"))
            }
        }
    }

    fun deletePoi(poi: PeclPoiEntity) {
        viewModelScope.launch {
            try {
                repository.deletePoi(poi)
                loadPoisForProgram(poi.programId)
            } catch (e: Exception) {
                (poisState as MutableLiveData).postValue(AppState.Error(e.message ?: "Cannot delete POI - tasks assigned"))
            }
        }
    }

    fun insertQuestion(question: PeclQuestionEntity, taskId: Long) {
        viewModelScope.launch {
            try {
                repository.insertQuestionWithAssignment(question, taskId)
                loadQuestionsForTask(taskId)
            } catch (e: Exception) {
                (questionsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error inserting question"))
            }
        }
    }

    fun updateQuestion(question: PeclQuestionEntity) {
        viewModelScope.launch {
            try {
                repository.updateQuestion(question)
                // Reload appropriate list
            } catch (e: Exception) {
                (questionsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Error updating question"))
            }
        }
    }

    fun deleteQuestion(question: PeclQuestionEntity) {
        viewModelScope.launch {
            try {
                repository.deleteQuestion(question)
                // Reload appropriate list
            } catch (e: Exception) {
                (questionsState as MutableLiveData).postValue(AppState.Error(e.message ?: "Cannot delete question - referenced elsewhere"))
            }
        }
    }

    fun insertEvaluationResult(result: PeclEvaluationResultEntity) {
        viewModelScope.launch {
            try {
                repository.insertEvaluationResult(result)
                // Reload if needed
            } catch (e: Exception) {
                // Handle error in UI via state if applicable
            }
        }
    }
}