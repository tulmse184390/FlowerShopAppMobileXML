package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.AITextResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

data class AIChatRequest(val prompt: String, val maxTokens: Int = 100)

interface AIChatService {
    @Headers("Authorization: Bearer YOUR_API_KEY", "Content-Type: application/json")
    @POST("https://api.openai.com/v1/completions")
    suspend fun getAIResponse(@Body request: AIChatRequest): Response<AITextResponse>
}