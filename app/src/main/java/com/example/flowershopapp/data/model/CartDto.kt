package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class CartDto(
    @SerializedName("cartId")
    val cartId: Int,

    @SerializedName("totalAmount")
    val totalAmount: Double,

    @SerializedName("items")
    val items: List<CartItemDto>
)