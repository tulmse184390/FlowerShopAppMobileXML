package com.example.flowershopapp.data.model

data class ChatMessage(
    @com.google.gson.annotations.SerializedName("senderRole")
    val user: String,
    val message: String
)