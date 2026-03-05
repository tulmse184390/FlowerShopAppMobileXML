package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class CartItemDto(
    @SerializedName("productId")
    val productId: Int,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("productImage")
    val productImage: String?,

    @SerializedName("price")
    val price: Double,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("subTotal")
    val subTotal: Double,

    var isSelected: Boolean = true
)