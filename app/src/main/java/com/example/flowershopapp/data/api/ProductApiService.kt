package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.ApiResponse
import com.example.flowershopapp.data.model.CategoryDto
import com.example.flowershopapp.data.model.PagedResult
import com.example.flowershopapp.data.model.ProductDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApiService {

    @GET("api/Categories")
    suspend fun getCategories(): Response<ApiResponse<List<CategoryDto>>>

    @GET("api/Products")
    suspend fun getProducts(
        @Query("Search") search: String? = null,
        @Query("CategoryId") categoryId: Int? = null,
        @Query("MinPrice") minPrice: Double? = null,
        @Query("MaxPrice") maxPrice: Double? = null,
        @Query("SortBy") sortBy: String? = null,
        @Query("PageIndex") pageIndex: Int = 1,
        @Query("PageSize") pageSize: Int = 6
    ): Response<ApiResponse<PagedResult<ProductDto>>>
}