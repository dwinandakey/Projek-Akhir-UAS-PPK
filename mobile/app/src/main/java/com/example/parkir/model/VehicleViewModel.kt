package com.example.parkir.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkir.network.ApiClient.apiService
import com.example.parkir.utils.SessionManager
import kotlinx.coroutines.launch

class VehicleViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _vehicles = MutableLiveData<List<Vehicle>>()
    val vehicles: LiveData<List<Vehicle>> = _vehicles

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchVehicles()
    }

    private fun getAuthHeader(): String {
        val token = sessionManager.getAuthToken()
        return "Bearer $token"
    }

    fun fetchVehicles() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getVehicles(getAuthHeader())
                if (response.isSuccessful) {
                    _vehicles.value = response.body()
                } else {
                    _error.value = "Failed to fetch vehicles: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateVehicle(id: Int, vehicle: Vehicle) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.updateVehicle(getAuthHeader(), id, vehicle)
                if (response.isSuccessful) {
                    fetchVehicles()
                } else {
                    _error.value = "Failed to update vehicle: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteVehicle(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.deleteVehicle(getAuthHeader(), id)
                if (response.isSuccessful) {
                    fetchVehicles()
                } else {
                    if (response.code() == 400) { // Conflict - Foreign Key Constraint
                        _error.value =
                            "Kendaraan tidak dapat dihapus karena masih memiliki data transaksi aktif. Hapus data transaksi terlebih dahulu."
                    } else {
                        _error.value = "Failed to delete vehicle: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is retrofit2.HttpException -> {
                        if (e.code() == 409) {
                            "Kendaraan tidak dapat dihapus karena masih memiliki data transaksi aktif. Hapus data transaksi terlebih dahulu."
                        } else {
                            "Error: ${e.message()}"
                        }
                    }

                    else -> e.message ?: "Unknown error occurred"
                }
                _error.value = errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}