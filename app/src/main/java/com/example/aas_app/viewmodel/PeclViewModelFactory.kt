package com.example.aas_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aas_app.data.AppRepository

class PeclViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeclViewModel::class.java)) {
            return PeclViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}