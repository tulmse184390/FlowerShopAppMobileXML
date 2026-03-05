package com.example.flowershopapp.ui.checkout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.data.model.CheckoutRequestDto
import com.example.flowershopapp.data.model.CheckoutResponseDto
import kotlinx.coroutines.launch

class CheckoutViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _checkoutResult = MutableLiveData<CheckoutResponseDto?>()
    val checkoutResult: LiveData<CheckoutResponseDto?> get() = _checkoutResult

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun placeOrder(token: String?, address: String, paymentMethod: String) {
        if (token.isNullOrEmpty()) {
            _errorMessage.value = "Please sign in!"
            return
        }
        if (address.isBlank()) {
            _errorMessage.value = "Enter the ship address!"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val request = CheckoutRequestDto(address, paymentMethod)
                val response = RetrofitClient.orderApi.checkout("Bearer $token", request)

                if (response.isSuccessful && response.body()?.success == true) {
                    // Thành công! Bắn dữ liệu (chứa link VNPay) ra ngoài Activity
                    _checkoutResult.value = response.body()?.data
                } else {
                    _errorMessage.value = response.body()?.message ?: "Payment failed!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error!"
            } finally {
                _isLoading.value = false
            }
        }
    }
}