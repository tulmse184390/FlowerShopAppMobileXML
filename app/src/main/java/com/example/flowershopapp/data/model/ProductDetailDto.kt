package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class ProductDetailDto(
    @SerializedName("productId")
    val productId: Int,

    @SerializedName("productName")
    val productName: String,

    @SerializedName("briefDescription")
    val briefDescription: String?,

    @SerializedName("fullDescription")
    val fullDescription: String?,

    @SerializedName("price")
    val price: Double,

    @SerializedName("stockQuantity")
    val stockQuantity: Int,

    @SerializedName("categoryName")
    val categoryName: String?,

    @SerializedName("images")
    val images: List<String>
)