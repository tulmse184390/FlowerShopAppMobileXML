package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class StoreLocationDto(
    @SerializedName("locationId")
    val locationId: Int,

    @SerializedName("storeName")
    val storeName: String?,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("address")
    val address: String?
)
