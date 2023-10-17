package com.example.commerce.books

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
    val categoryName: String
)

data class BookCommentCountResponse (
    val id: Long,
    val comment: String,
    val createdDate: String,
    val profileId: Long,
    val nickname: String,
    val commentCount: Long
)

data class BookCreateRequest( val comment: String )

fun BookCreateRequest.validate() : Boolean {
    return this.comment.isNotEmpty()
}

data class BookCommentModifyRequest(val comment: String)