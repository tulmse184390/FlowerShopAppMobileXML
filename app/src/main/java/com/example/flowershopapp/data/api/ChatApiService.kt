package com.example.flowershopapp.data.api

import com.example.flowershopapp.data.model.ChatMessage
import com.example.flowershopapp.data.model.ChatRoomDto
import com.example.flowershopapp.data.model.ToggleAIResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatApiService {
    @GET("api/Chat/chat_room")
    suspend fun getChatRooms(@Header("Authorization") token: String): Response<List<ChatRoomDto>>

    @GET("api/Chat/history")
    suspend fun getMyHistory(@Header("Authorization") token: String): Response<List<ChatMessage>>

    @GET("api/Chat/{roomId}/history")
    suspend fun getHistoryByRoomId(@Header("Authorization") token: String, @Path("roomId") roomId: Int): Response<List<ChatMessage>>

    @POST("api/Chat/{roomId}/takeover")
    suspend fun takeoverRoom(@Header("Authorization") token: String, @Path("roomId") roomId: Int): Response<ToggleAIResponse>

    @POST("api/Chat/{roomId}/release")
    suspend fun releaseRoom(@Header("Authorization") token: String, @Path("roomId") roomId: Int): Response<ToggleAIResponse>
}
