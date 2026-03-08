package com.example.flowershopapp.data.model

data class AITextResponse(
    val choices: List<Choice>
) {
    data class Choice(
        val text: String
    )
}