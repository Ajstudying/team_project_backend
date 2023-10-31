package com.example.commerce.admin

data class TodayDataResponse(
    val cover: String,
    val title: String,
    val author: String,
    val priceSales: Int,
    val todayLetter: String,
    val itemId: Int,
    val readDate: String,
)

data class TodayLetterResponse(
        val id: Long,
        val cover: String,
        val title: String,
        val author: String,
        val priceSales: Int,
        val todayLetter: String,
        val itemId: Int,
        val readDate: String,
)