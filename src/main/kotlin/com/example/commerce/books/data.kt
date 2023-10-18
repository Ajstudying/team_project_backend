package com.example.commerce.books

import org.jetbrains.exposed.dao.id.EntityID

interface SeriesInfo {
    val seriesId: Int
    val seriesLink: String
    val seriesName: String
}
data class NewBookRequest(
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
    val seriesInfo: SeriesInfo,
)


data class BookResponse (
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
    val commentCount: Long
)
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
    val item: List<NewBookResponse>
)

data class NewBookResponse (
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
)

data class SeriesInfoResponse (
    val seriesId: Int,
    val seriesLink: String,
    val seriesName: String
)

data class BookListResponse (
    val id: Long,
    val publisher: String,
    val title: String,
    val link: String,
    val author: String,
    val pubDate: String,
    val description: String,
    val itemId: Int,
    val priceSales: Int,
    val priceStandard: Int,
    val stockStatus: String,
    val cover: String,
    val categoryId: Int,
    val categoryName: String,
    val commentCount: Long
)

data class BookBestResponse (
    val id: Long,
    val publisher: String,
    val title: String,
    val link: String,
    val author: String,
    val pubDate: String,
    val description: String,
    val itemId: Int,
    val priceSales: Int,
    val priceStandard: Int,
    val stockStatus: String,
    val cover: String,
    val categoryId: Int,
    val categoryName: String,
)


data class BookCommentCountResponse (
    val id: Long,
    val title: String,
    val categoryName: String,
    val author: String,
    val publisher: String,
    val commentCount: Long
)

data class BookCreateRequest( val comment: String )

fun BookCreateRequest.validate() : Boolean {
    return this.comment.isNotEmpty()
}

data class BookCommentModifyRequest(val comment: String)