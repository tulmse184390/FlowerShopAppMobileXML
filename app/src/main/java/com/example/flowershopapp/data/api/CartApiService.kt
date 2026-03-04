package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.AddToCartRequestDto
import com.example.flowershopapp.data.model.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface CartApiService {
    @POST("api/Cart/add")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Body request: AddToCartRequestDto
    ): Response<ApiResponse<String>>
}