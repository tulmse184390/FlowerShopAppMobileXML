package com.example.flowershopapp.data.model

data class ChatRoomDto(
    val roomId: Int,
    val name: String?,
    val userId: Int,
    var isAIAssisted: Boolean,
    val isActive: Boolean,
    val createdAt: String?
)

data class ToggleAIResponse(val message: String)
