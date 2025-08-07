package com.example.aas_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aas_app.data.AppRepository

class DemographicsViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DemographicsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DemographicsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}