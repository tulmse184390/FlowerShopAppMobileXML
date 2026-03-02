package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.ApiResponse
import com.example.flowershopapp.data.model.AuthResponseDto
import com.example.flowershopapp.data.model.LoginRequestDto
import com.example.flowershopapp.data.model.RegisterRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/Auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<ApiResponse<AuthResponseDto>>

    @POST("api/Auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<ApiResponse<AuthResponseDto>>
}