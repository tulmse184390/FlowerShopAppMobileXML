package com.example.flowershopapp.data.model

import com.google.gson.annotations.SerializedName

data class CheckoutRequestDto(
    @SerializedName("shippingAddress")
    val shippingAddress: String,

    @SerializedName("paymentMethod")
    val paymentMethod: String = "VNPAY"
)