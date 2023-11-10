package com.example.commerce.admin

import com.example.commerce.admin.MainFiles.uniqueIndex
import org.springframework.web.multipart.MultipartFile

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
    val image: String,
    val link :String,
)

data class StockStatusResponse(
    val itemId: String,
    val stockStatus: String,
)