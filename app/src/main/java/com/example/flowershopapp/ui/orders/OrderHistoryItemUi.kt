package com.example.flowershopapp.ui.orders

data class OrderHistoryItemUi(
    val orderId: Int?,
    val status: String,
    val orderDateText: String,
    val totalText: String
)
