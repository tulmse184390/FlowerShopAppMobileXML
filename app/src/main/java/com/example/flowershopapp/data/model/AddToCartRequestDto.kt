package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class AddToCartRequestDto(
    @SerializedName("productId")
    val productId: Int,

    @SerializedName("quantity")
    val quantity: Int
)