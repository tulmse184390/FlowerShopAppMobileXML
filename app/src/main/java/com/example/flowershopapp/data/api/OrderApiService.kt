package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.ApiResponse
import com.example.flowershopapp.data.model.CheckoutRequestDto
import com.example.flowershopapp.data.model.CheckoutResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OrderApiService {
    @POST("api/Orders/checkout")
    suspend fun checkout(
        @Header("Authorization") token: String,
        @Body request: CheckoutRequestDto
    ): Response<ApiResponse<CheckoutResponseDto>>
}