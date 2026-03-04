package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("categoryId")
    val categoryId: Int,

    @SerializedName("categoryName")
    val categoryName: String,

    @SerializedName("description")
    val description: String?
)