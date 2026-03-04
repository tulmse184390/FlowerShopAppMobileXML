package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("productId")
    val productId: Int,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("briefDescription")
    val briefDescription: String?,

    @SerializedName("price")
    val price: Double,

    @SerializedName("categoryName")
    val categoryName: String,

    @SerializedName("imageUrl")
    val imageUrl: String?
)