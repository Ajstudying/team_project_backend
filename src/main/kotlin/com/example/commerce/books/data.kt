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
    val categoryName: String,
    val commentCount: Long,
    val bookComment : List<BookCommentResponse>,
    val likedBook: List<LikedBookResponse>,
)

data class BookCommentResponse (
    val id: Long,
    val comment: String,
    val nickname: String,
    val createdDate: Long,
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
    val commentCount: Long,
    val likeBooks: List<LikedBookResponse>
)

data class LikedBookResponse (
    val id: Long,
    val profileId: Long,
    val likes: Boolean,
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
    val customerReviewRank : Int,
)



data class CreateCommentRequest( val new: Int?, val comment: String, val createdDate: Long )

fun CreateCommentRequest.validate() : Boolean {
    return this.comment.isNotEmpty()
}

data class BookCommentModifyRequest( val comment: String )

data class CreateLikeRequest(val new: Int?, val like: Boolean)