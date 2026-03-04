package com.example.flowershopapp.ui.products

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.data.model.AddToCartRequestDto
import com.example.flowershopapp.data.model.ProductDetailDto
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _productDetail = MutableLiveData<ProductDetailDto?>()
    val productDetail: LiveData<ProductDetailDto?> get() = _productDetail

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _addToCartSuccess = MutableLiveData<String?>()
    val addToCartSuccess: LiveData<String?> get() = _addToCartSuccess

    fun addToCart(token: String?, productId: Int, quantity: Int) {
        if (token.isNullOrEmpty()) {
            _errorMessage.value = "Please sign in to buy!"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        _addToCartSuccess.value = null

        viewModelScope.launch {
            try {
                val request = AddToCartRequestDto(productId, quantity)
                val authHeader = "Bearer $token"

                val response = RetrofitClient.cartApi.addToCart(authHeader, request)

                if (response.isSuccessful && response.body()?.success == true) {
                    _addToCartSuccess.value = response.body()?.message ?: "Add to cart successfully!"
                } else {
                    _errorMessage.value = response.body()?.message ?: "Add to cart failed!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error!"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchProductDetail(id: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.productApi.getProductDetail(id)
                if (response.isSuccessful && response.body()?.success == true) {
                    _productDetail.value = response.body()?.data
                } else {
                    _errorMessage.value = response.body()?.message ?: "Image error!"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Server error!"
            } finally {
                _isLoading.value = false
            }
        }
    }
}