package com.example.aas_app.util

import android.util.Log

sealed class AppState<out T> {
    object Loading : AppState<Nothing>()
    data class Success<out T>(val data: T) : AppState<T>()
    data class Error(val message: String) : AppState<Nothing>()
}