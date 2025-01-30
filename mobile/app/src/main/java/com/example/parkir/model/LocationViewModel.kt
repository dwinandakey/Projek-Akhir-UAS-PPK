package com.example.parkir.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkir.network.ApiClient.apiService
import com.example.parkir.utils.SessionManager
import kotlinx.coroutines.launch

class LocationViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _locations = MutableLiveData<List<ParkingLocation>>()
    val locations: LiveData<List<ParkingLocation>> = _locations

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchLocations()
    }

    private fun getAuthHeader(): String {
        val token = sessionManager.getAuthToken()
        return "Bearer $token"
    }

    fun fetchLocations() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getLocations(getAuthHeader())
                if (response.isSuccessful) {
                    _locations.value = response.body()
                } else {
                    _error.value = "Failed to fetch locations: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addLocation(location: ParkingLocation) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.addLocation(getAuthHeader(), location)
                if (response.isSuccessful) {
                    fetchLocations()
                } else {
                    _error.value = "Failed to add location: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLocation(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.deleteLocation(getAuthHeader(), id)
                if (response.isSuccessful) {
                    fetchLocations()
                } else {
                    _error.value = "Failed to delete location: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateLocation(id: Int, location: ParkingLocation) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.updateLocation(getAuthHeader(), id, location)
                if (response.isSuccessful) {
                    fetchLocations()
                } else {
                    _error.value = "Failed to update location: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
}