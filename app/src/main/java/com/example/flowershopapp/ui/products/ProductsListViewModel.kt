package com.example.flowershopapp.ui.products

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowershopapp.data.api.RetrofitClient
import com.example.flowershopapp.data.model.CategoryDto
import com.example.flowershopapp.data.model.PagedResult
import com.example.flowershopapp.data.model.ProductDto
import kotlinx.coroutines.launch

class ProductsListViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _categories = MutableLiveData<List<CategoryDto>>()
    val categories: LiveData<List<CategoryDto>> get() = _categories

    private val _productsData = MutableLiveData<PagedResult<ProductDto>>()
    val productsData: LiveData<PagedResult<ProductDto>> get() = _productsData

    var currentSearch: String? = null
    var currentCategoryId: Int? = null
    var currentMinPrice: Double? = null
    var currentMaxPrice: Double? = null
    var currentSortBy: String? = null
    var currentPageIndex: Int = 1

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.productApi.getCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    val apiCategories = response.body()?.data ?: emptyList()

                    val allCategory = CategoryDto(categoryId = -1, categoryName = "All", description = "")

                    val combinedList = listOf(allCategory) + apiCategories

                    _categories.value = combinedList
                }
            } catch (e: Exception) {
                Log.e("ProductsListVM", "Category Error: ${e.message}")
            }
        }
    }

    fun fetchProducts(isRefresh: Boolean = true) {
        if (isRefresh) currentPageIndex = 1

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val response = RetrofitClient.productApi.getProducts(
                    search = currentSearch,
                    categoryId = currentCategoryId,
                    minPrice = currentMinPrice,
                    maxPrice = currentMaxPrice,
                    sortBy = currentSortBy,
                    pageIndex = currentPageIndex
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { pagedData ->
                        _productsData.value = pagedData
                    }
                } else {
                    _errorMessage.value = "List of products error!"
                }
            } catch (e: Exception) {
                Log.e("ProductsListVM", "Product error: ${e.message}")
                _errorMessage.value = "Server error!"
            } finally {
                _isLoading.value = false
            }
        }
    }
}