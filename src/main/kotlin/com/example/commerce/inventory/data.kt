package com.example.commerce.inventory

data class TodayLetterResponse(
    val id: Long,
    val cover: String,
    val title: String,
    val author: String,
    val priceSales: Int,
    val todayLetter: String,
    val bookId: Long,
)