package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.ApiResponse
import com.example.flowershopapp.data.model.CheckoutRequestDto
import com.example.flowershopapp.data.model.CheckoutResponseDto
import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApiService {
    @GET("api/Orders")
    suspend fun getOrders(
        @Header("Authorization") token: String
    ): Response<JsonElement>

    @GET("api/Orders/{id}")
    suspend fun getOrderById(
        @Header("Authorization") token: String,
        @Path("id") orderId: Int
    ): Response<JsonElement>

    @POST("api/Orders/checkout")
    suspend fun checkout(
        @Header("Authorization") token: String,
        @Body request: CheckoutRequestDto
    ): Response<ApiResponse<CheckoutResponseDto>>
}