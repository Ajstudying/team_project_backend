package com.example.commerce.publisher

data class EventBookResponse(
        val id: Long,
        val itemId: Int,
        val title: String,
        val description: String,
        val cover: String,
        val textSentence: String,
        val mentSentence: String,
        val authorImage: String,
        val author: String,
        val publisher: String,
        val authorDescription: String,

        )

