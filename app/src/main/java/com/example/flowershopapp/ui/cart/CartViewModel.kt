package com.example.flowershopapp.ui.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.data.model.CartDto
import com.example.flowershopapp.data.model.UpdateCartDto
import kotlinx.coroutines.launch

class CartViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _cartData = MutableLiveData<CartDto?>()
    val cartData: LiveData<CartDto?> get() = _cartData

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchCart(token: String?) {
        if (token.isNullOrEmpty()) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.cartApi.getMyCart("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    _cartData.value = response.body()?.data
                } else {
                    _errorMessage.value = response.body()?.message ?: "Cart error!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateQuantity(token: String?, productId: Int, newQuantity: Int) {
        if (token.isNullOrEmpty()) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val request = UpdateCartDto(productId, newQuantity)
                val response = RetrofitClient.cartApi.updateQuantity("Bearer $token", request)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchCart(token)
                } else {
                    _errorMessage.value = response.body()?.message ?: "Update failed!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error!"
            }
        }
    }

    fun removeItem(token: String?, productId: Int) {
        if (token.isNullOrEmpty()) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.cartApi.removeItem("Bearer $token", productId)
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchCart(token) // Xóa xong tải lại
                } else {
                    _errorMessage.value = "Can not delete this cart item!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error!"
            }
        }
    }

    fun clearCart(token: String?) {
        if (token.isNullOrEmpty()) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.cartApi.clearCart("Bearer $token")
                if (response.isSuccessful && response.body()?.success == true) {
                    fetchCart(token)
                } else {
                    _errorMessage.value = "Clear error!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error!"
            }
        }
    }
}