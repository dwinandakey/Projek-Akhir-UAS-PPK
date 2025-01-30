package com.example.parkir.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkir.network.ApiClient
import com.example.parkir.utils.SessionManager
import kotlinx.coroutines.launch
import java.time.LocalDate

class TransactionViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val _transactions = MutableLiveData<List<TransaksiParkirResponse>>()
    val transactions: LiveData<List<TransaksiParkirResponse>> = _transactions

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    private fun getAuthHeader(): String {
        return "Bearer ${sessionManager.getAuthToken()}"
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        startDate = start
        endDate = end
        fetchTransactions()
    }

    fun fetchTransactions() {
        if (startDate == null || endDate == null) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = ApiClient.apiService.searchTransactions(
                    getAuthHeader(),
                    startDate!!.toString(),
                    endDate!!.toString()
                )
                if (response.isSuccessful) {
                    _transactions.value = response.body()
                } else {
                    _error.value = "Failed to fetch transactions"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            try {
                val response = ApiClient.apiService.deleteTransaksiParkir(getAuthHeader(), id)
                if (response.isSuccessful) {
                    fetchTransactions()
                } else {
                    _error.value = "Failed to delete transaction"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}