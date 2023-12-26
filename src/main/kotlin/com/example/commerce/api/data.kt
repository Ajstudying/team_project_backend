package com.example.commerce.api

//알라딘 api 상품 연결 응답 version 데이터 형식
// 리스트 연결 방식 및 필요 데이터 선정 과정이 좀 까다로웠으나 배열로 된 것은
// 리스트 형식으로 받는 걸로 해결
data class NewBookDataResponse (
    val version: String,
    val logo: String,
    val title: String,
    val link: String,
    val pubDate: String,
    val totalResults: String,
    val startIndex: Int,
    val itemsPerPage: Int,
    val query: String,
    val searchCategoryId: Int,
    val searchCategoryName: String,
    val item: List<BookDataResponse>
)
//알라딘 api 상품 연결 응답 item 데이터 형식
data class BookDataResponse (
    val id: Long,
    val publisher: String,
    val title: String,
    val link: String,
    val author: String,
    val pubDate: String,
    val description: String,
    val isbn: String,
    val isbn13: String,
    val itemId: Int,
    val priceSales: Int,
    val priceStandard: Int,
    val stockStatus: String,
    val cover: String,
    val categoryId: Int,
    val categoryName: String,
    val customerReviewRank : Int,

)
//알라딘 검색 api 연결 응답 연결 데이터 형식 version
data class SearchResponse (
        val version: String,
        val logo: String,
        val title: String,
        val link: String,
        val pubDate: String,
        val totalResults: Int,
        val startIndex: Int,
        val itemsPerPage: Int,
        val query: String,
        val searchCategoryId: Int,
        val searchCategoryName: String,
        val item: List<SearchDataResponse>
)
//알라딘 검색 api 연결 응답 연결 데이터 형식 item
//시리즈가 있을 경우에는 받을 수 있고, 없을 때는 안 받아도 되게끔 SeriesInfo? 으로 받기
//저 형식을 제대로 설정하지 않으면 오류가 생기거나 시리즈 데이터 자체가 안 받아짐.
data class SearchDataResponse(
        val title: String,
        val link: String,
        val author: String,
        val pubDate: String,
        val description: String,
        val isbn: String,
        val isbn13: String,
        val itemId: Int,
        val priceSales: Int,
        val priceStandard: Int,
        val stockStatus: String,
        val cover: String,
        val categoryId: Int,
        val categoryName: String,
        val publisher: String,
        val customerReviewRank: Int,
        val seriesInfo: SeriesInfo?,
)
//알라딘 검색 api 연결 응답 연결 데이터 형식 seriesInfo
//시리즈가 있을 경우에
data class SeriesInfo(
        val seriesId: Int,
        val seriesLink: String,
        val seriesName: String
)

