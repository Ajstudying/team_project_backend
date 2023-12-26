package com.example.commerce.admin

import com.example.commerce.admin.MainFiles.uniqueIndex
import org.springframework.web.multipart.MultipartFile

//오늘의 책 백오피스에서 들어오는 응답 데이터 형식
data class TodayDataResponse(
    val cover: String,
    val title: String,
    val author: String,
    val priceSales: Int,
    val todayLetter: String,
    val itemId: Int,
    val readDate: String,
)

//도서몰 페이지에 내보내는 데이터 형식
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

//백오피스에 보내주는 조회수 데이터 형식
data class HitsDataResponse(
    val itemId: Int,
    val hitsCount: Long,
    val createDate: String,
)

//백오피스에서 받는 파일 이미지 응답 데이터
data class MainFileResponse(
    val image: String,
    val link :String,
)

//백오피스에서 받는 재고 응답 데이터
data class StockStatusResponse(
    val itemId: String,
    val stockStatus: String,
)