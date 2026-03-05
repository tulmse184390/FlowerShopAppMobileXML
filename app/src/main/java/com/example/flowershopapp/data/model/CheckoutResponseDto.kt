package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class CheckoutResponseDto(
    @SerializedName("orderId")
    val orderId: Int,

    @SerializedName("paymentUrl")
    val paymentUrl: String?
)