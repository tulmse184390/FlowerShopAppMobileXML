package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class UpdateCartDto(
    @SerializedName("productId")
    val productId: Int,

    @SerializedName("quantity")
    val quantity: Int
)