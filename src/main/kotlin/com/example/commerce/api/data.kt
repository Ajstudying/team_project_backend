package com.example.commerce.api


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

data class SeriesInfo(
        val seriesId: Int,
        val seriesLink: String,
        val seriesName: String
)

