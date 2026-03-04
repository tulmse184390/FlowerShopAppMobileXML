package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class PagedResult<T>(
    @SerializedName("currentPage")
    val currentPage: Int,

    @SerializedName("totalPages")
    val totalPages: Int,

    @SerializedName("pageSize")
    val pageSize: Int,

    @SerializedName("totalCount")
    val totalCount: Int,

    @SerializedName("items")
    val items: List<T>
)