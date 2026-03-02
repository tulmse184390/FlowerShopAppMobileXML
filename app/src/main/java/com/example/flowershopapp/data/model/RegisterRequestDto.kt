package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto (
    @SerializedName("userName")
    val userName: String,

    @SerializedName(value = "password")
    val password: String,

    @SerializedName(value = "fullName")
    val fullName: String,

    @SerializedName(value = "email")
    val email: String,

    @SerializedName(value = "phoneNumber")
    val phoneNumber: String,

    @SerializedName(value = "address")
    val address: String
)