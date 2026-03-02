package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponseDto(
    @SerializedName(value = "userid")
    val userId: Int,

    @SerializedName(value = "userName")
    val userName: String,

    @SerializedName(value = "fullNamee")
    val fullName: String,

    @SerializedName(value = "email")
    val email: String,

    @SerializedName(value = "phoneNumber")
    val phoneNumber: String,

    @SerializedName(value = "address")
    val address: String,

    @SerializedName(value = "role")
    val role: String,

    @SerializedName("token")
    val token: String
)