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

data class HitsDataResponse(
    val itemId: Int,
    val nickname: String?,
    val birth: Int?,
    val bookmark: String?,
    val hitsCount: Long,
    val createDate: String,
    val gender: Int?,
)

data class MainFileResponse(
    val id: Long,
    val uuidFileName: String,
    val originalFileName: String,
    val contentType: String,
)