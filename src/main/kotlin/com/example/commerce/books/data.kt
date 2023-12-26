package com.example.commerce.books

//도서몰에 내보내는 도서 데이터 형태
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
//도서몰에 내보낼 댓글 형태
data class BookCommentResponse (
    val id: Long,
    val comment: String,
    val nickname: String,
    val createdDate: Long,
    val replyComment: List<ReplyCommentResponse>
)

//저장할 댓글 형태
data class SaveBookCommentResponse (
        val id: Long,
        val comment: String,
        val nickname: String,
        val createdDate: Long,
)

//저장할 답글 형태 /내보낼 답글 형태
data class ReplyCommentResponse (
    val id: Long,
    val comment: String,
    val nickname: String,
    val createdDate: Long,
    val parentId : Long,
)
//도서몰에 내보내는 베스트셀러 데이터 형태
data class BookBestResponse (
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
    val likedBook: List<LikedBookResponse>
)
//국내도서/외국도서 데이터 형태
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
    val likedBook: List<LikedBookResponse>
)
//선호작품 데이터 형태
data class LikedBookResponse (
    val id: Long,
    val profileId: Long,
    val likes: Boolean,
)
//알림 설정 데이터 형태
data class AlamBookResponse(
    val bookItemId: Int,
    val profileId: Long,
    val alamDisplay: Boolean,
    val alam: Boolean,
    val bookTitle: String,
)
//댓글 만드는 요청 데이터 형태
data class CreateCommentRequest( val new: Int?, val comment: String, val createdDate: Long )
//댓글 인증
fun CreateCommentRequest.validate() : Boolean {
    return this.comment.isNotEmpty()
}
//수정 댓글 요청 데이터 형태
data class BookCommentModifyRequest( val comment: String )
//선호작품 만드는 요청 데이터 형태
data class CreateLikeRequest(val new: Int?, val like: Boolean)
//알림 설정 만드는 요청 데이터 형태
data class CreateAlamRequest(val alamDisplay: Boolean)