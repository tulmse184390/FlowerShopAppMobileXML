package com.example.flowershopapp.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.data.model.StoreLocationDto
import kotlinx.coroutines.launch

class MapViewModel : ViewModel() {

    private val _stores = MutableLiveData<List<StoreLocationDto>>()
    val stores: LiveData<List<StoreLocationDto>> = _stores

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchStores() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.storeApi.getStores()
                if (response.isSuccessful) {
                    _stores.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load stores"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Network error"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
