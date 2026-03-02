package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("userName")
    val userName: String,

    @SerializedName("password")
    val password: String
)
