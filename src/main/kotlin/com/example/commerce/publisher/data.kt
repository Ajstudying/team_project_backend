package com.example.commerce.publisher

data class EventBookResponse(
    val id: Long,
    val itemId: Int,
    val title: String,
    val author: String,
    val authorImage: String,
    val authorDesc: String,
    val cover: String,
    val descTitle: String,
    val description: String,
    val descCover: String,
    val publisher: String,
)

