package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.AddToCartRequestDto
import com.example.flowershopapp.data.model.ApiResponse
import com.example.flowershopapp.data.model.CartDto
import com.example.flowershopapp.data.model.UpdateCartDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CartApiService {
    @POST("api/Cart/add")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Body request: AddToCartRequestDto
    ): Response<ApiResponse<String>>

    @GET("api/Cart")
    suspend fun getMyCart(
        @Header("Authorization") token: String
    ): Response<ApiResponse<CartDto>>

    @PUT("api/Cart/update")
    suspend fun updateQuantity(
        @Header("Authorization") token: String,
        @Body request: UpdateCartDto
    ): Response<ApiResponse<String>>

    @DELETE("api/Cart/remove/{productId}")
    suspend fun removeItem(
        @Header("Authorization") token: String,
        @Path("productId") productId: Int
    ): Response<ApiResponse<String>>

    @DELETE("api/Cart/clear")
    suspend fun clearCart(
        @Header("Authorization") token: String
    ): Response<ApiResponse<String>>
}