package com.example.parkir.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.parkir.utils.SessionManager

class VehicleViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VehicleViewModel(sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}