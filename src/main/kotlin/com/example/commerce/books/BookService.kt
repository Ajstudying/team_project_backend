package com.example.commerce.books


import com.example.commerce.auth.Profiles
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping

data class BookTable (
    val id: Column<EntityID<Long>>,
    val publisher: Column<String>,
    val title: Column<String>,
    val link: Column<String>,
    val author: Column<String>,
    val pubDate: Column<String>,
    val description: Column<String>,
    val isbn: Column<String>,
    val isbn13: Column<String>,
    val itemId: Column<Int>,
    val priceSales: Column<Int>,
    val priceStandard: Column<Int>,
    val stockStatus:Column<String>,
    val cover: Column<String>,
    val categoryId: Column<Int>,
    val categoryName: Column<String>,
    val customerReviewRank : Column<Int>,
)

@FeignClient(name="books", url="http://192.168.100.36:8081/books")
interface MyBooksClient {

    @GetMapping("/book-list")
    fun getBooksData() : List<BookDataResponse>
}
@Service
class BookService
    (private val myBooksClient: MyBooksClient,
     private val redisTemplate: RedisTemplate<String, String>)
{

    private val mapper = jacksonObjectMapper()
    @Scheduled(cron = "0 0 0 1 * ?")
    fun scheduledFetchBooksData() {
        println("--- booksData fetching ---")
        val items = myBooksClient.getBooksData()

        //결과값 저장
        redisTemplate.delete("book-list")

        items.forEach{ data ->
            // 책 데이터를 JSON 형태로 변환

            val key = "book:${data.id}" // 각 도서에 대한 고유한 키 생성
            val bookJson = mapper.writeValueAsString(data)
            redisTemplate.opsForValue().set(key, bookJson) // 해당 키에 도서 정보 저장
        }
    }

    fun getCachedBookList(): List<BookDataResponse> {

        // 전체 데이터 키 목록 가져오기
        val bookKeys = redisTemplate.keys("book:*")

        val books: List<String>? = redisTemplate.opsForValue().multiGet(bookKeys)

        if (!books.isNullOrEmpty()) {
            val combinedJson = books.joinToString(",")
            return mapper.readValue("[$combinedJson]", object : TypeReference<List<BookDataResponse>>() {})
        } else {
            return emptyList() // 데이터가 없는 경우 빈 리스트 반환
        }
    }


    //카운트별칭
    fun getComment(): ExpressionAlias<Long> {
        val c = BookComments
        return c.id.count().alias("commentCount")
    }

    //신간 도서 댓글 리스트 찾기
    fun getNewBooksComments(id: Long): List<BookCommentResponse> {
        val c = BookComments
        val pf = Profiles;
        val comments = transaction {
            (c innerJoin pf)
                .select{(c.newBookId eq id)}
                .orderBy(c.id to SortOrder.DESC)
                .mapNotNull { r -> BookCommentResponse (
                    r[c.id].value, r[c.comment],r[pf.nickname], r[c.createdDate],
                ) }
        }
        return comments
    }
    //신간 도서 상세 찾기
    fun getNewBook(id: Long, comments:  List<BookCommentResponse>, likes: List<LikedBookResponse>): BookResponse? {
        val n = NewBooks

        val newBook = transaction {
            n.select { (NewBooks.id eq id) }
                .groupBy(n.id)  // groupBy 메소드로 그룹화할 기준 컬럼들을 지정
                .mapNotNull { r ->
                    //집계 함수식의 별칭 설정
                    val commentCount = comments.size.toLong()
                    BookResponse(
                        r[n.id].value, r[n.publisher], r[n.title], r[n.link], r[n.author], r[n.pubDate],
                        r[n.description], r[n.isbn], r[n.isbn13], r[n.itemId], r[n.priceSales],
                        r[n.priceStandard], r[n.stockStatus], r[n.cover], r[n.categoryId], r[n.categoryName],
                        commentCount= commentCount, bookComment = comments, likedBook = likes,
                    ) }
                .singleOrNull() }

        return newBook
    }

    //신간 댓글 찾기
    fun findComment (id: Long, profileId: Long) : ResultRow? {
        val comment = transaction {
            BookComments
                .select (where = (BookComments.id eq id) and
                        (BookComments.profileId eq profileId)).firstOrNull() }
        return comment
    }

    //좋아요 추가/수정
    fun updateLikeRecord (id: Long, profileId: Long, likes: Boolean, newBookId: Int?): ResponseEntity<Any> {
        try {
            // 좋아요가 이미 존재하는지 확인
            val findLike = transaction {
                val table = if (newBookId != null) LikeBooks.newBookId else LikeBooks.bookId
                LikeBooks
                    .select { (table eq id) and (LikeBooks.profileId eq profileId) }
                    .firstOrNull()
            }

            // 좋아요가 이미 존재할 때
            if (findLike != null) {
                println("$likes 좋아요인가")
                transaction {
                    val table = if (newBookId != null) LikeBooks.newBookId else LikeBooks.bookId
                    LikeBooks.update({ (table eq id) and (LikeBooks.profileId eq profileId) }) {
                        it[LikeBooks.likes] = likes
                    }
                }
            } else {
                println("$likes 새로 생긴 좋아요인가")
                transaction {
                    LikeBooks.insert {
                        it[LikeBooks.likes] = likes
                        it[LikeBooks.profileId] = profileId
                        if (newBookId != null) {
                            it[LikeBooks.newBookId] = id
                        } else {
                            it[LikeBooks.bookId] = id
                        }
                    }
                }
            }

            // 성공적으로 업데이트나 삽입을 마친 후 OK 응답 반환
            return ResponseEntity.status(HttpStatus.OK).build()
        } catch (e: Exception) {
            // 예외가 발생한 경우 NOT_FOUND 응답 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    //매핑 함수
//    fun mapToBookResponse(
//        table: BookTable, r: ResultRow, commentCountAlias: ExpressionAlias<Long>
//    ): BookResponse {
//        return BookResponse(
//            r[table.id].value,
//            r[table.publisher],
//            r[table.title],
//            r[table.link],
//            r[table.author],
//            r[table.pubDate],
//            r[table.description],
//            r[table.isbn],
//            r[table.isbn13],
//            r[table.itemId],
//            r[table.priceSales],
//            r[table.priceStandard],
//            r[table.stockStatus],
//            r[table.cover],
//            r[table.categoryId],
//            r[table.categoryName],
//            r[commentCountAlias]
//        )
//    }


}