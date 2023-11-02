package com.example.commerce.books


import com.example.commerce.admin.AdminService
import com.example.commerce.admin.HitsDataResponse
import com.example.commerce.auth.AuthProfile
import com.example.commerce.auth.Profiles
import com.example.commerce.admin.HitsTable
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter



@Service
class BookService
    (private val redisTemplate: RedisTemplate<String, String>,
     private val adminService: AdminService)
//     private val redisTemplate: RedisTemplate<Long, String>)
{
    private val mapper = jacksonObjectMapper()

    fun getCachedBookList(): List<BookDataResponse> {
        val result = redisTemplate.opsForValue().get("book-list")
        return if(result != null){
            mapper.readValue(result)
        }else{
            listOf()
        }
        // 전체 데이터 키 목록 가져오기
//        val bookKeys = redisTemplate.keys("book:*")
//        val books: List<String>? = redisTemplate.opsForValue().multiGet(bookKeys)
//
//        if (!books.isNullOrEmpty()) {
//            val combinedJson = books.joinToString(",")
//            return mapper.readValue("[$combinedJson]", object : TypeReference<List<BookDataResponse>>() {})
//        } else {
//            return emptyList() // 데이터가 없는 경우 빈 리스트 반환
//        }
    }

    fun getNewCategory(option: String): List<BookDataResponse> {
        val result = redisTemplate.opsForValue().get(option)
        return if(result != null) {
            mapper.readValue(result)
        }else{
            listOf()
        }
    }

    //카운트별칭
    fun getCommentCount(): ExpressionAlias<Long> {
        val c = BookComments
        return c.id.count().alias("commentCount")
    }

    //신간 도서 상세 찾기
    fun getNewBook(id: Long, profileId: Long?): BookResponse? {
        val n = NewBooks
        val c = BookComments
        val pf = Profiles
        val r = ReplyComments

        //답글 찾기
        val reply : List<ReplyCommentResponse> = transaction {
            (r innerJoin c).join (pf, JoinType.LEFT, onColumn = pf.id, otherColumn = r.profileId)
                .select { (r.bookId eq id) and (r.bookCommentId eq c.id) }
                .orderBy(r.id to SortOrder.DESC)
                .mapNotNull { row ->
                    ReplyCommentResponse(
                        row[r.id].value, row[r.comment], row[pf.nickname]
                        , row[r.createdDate], row[c.id].value
                    )
                }
        }
        println(reply+"신간 replyComments ------------------")

        //댓글 찾기
        val comments: List<BookCommentResponse> = transaction {
            (c innerJoin pf leftJoin r)
                .slice(c.columns + c.id + pf.id + pf.nickname + r.columns)
                .select{(c.newBookId eq id)}
                .orderBy(c.id to SortOrder.DESC)
                .mapNotNull { r ->
                    val commentReplies = reply.filter { replyComment ->
                        replyComment.parentId == r[c.id].value
                    }
                    BookCommentResponse (
                        r[c.id].value, r[c.comment], r[pf.nickname],
                        r[c.createdDate], replyComment = commentReplies
                    ) }
        }
        //선호작품 찾기
        val likes = transaction {
            (LikeBooks innerJoin Profiles)
                .select { LikeBooks.newBookId eq id }
                .mapNotNull { r -> LikedBookResponse(
                    r[LikeBooks.id].value,r [Profiles.id].value, r[LikeBooks.likes],
                ) }
        }

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

        val itemId = n.select { n.id eq id }.singleOrNull()?.get(n.itemId)?.toInt()
        if(itemId != null){
            //조회수 테이블 만들기
            adminService.sendRabbitData(itemId, profileId)
        }

        return newBook
    }


    //도서 상세 조회
    fun getBooks (id: Long, profileId: Long?) : BookResponse? {
        val b = Books
        val c = BookComments
        val pf = Profiles
        val r = ReplyComments

//        val slices = (b leftJoin (c innerJoin pf))
//                .slice(b.columns + c.id + commentCount + pf.id)

        //답글 찾기
        val reply : List<ReplyCommentResponse> = transaction {
            (r innerJoin c).join (pf, JoinType.LEFT, onColumn = pf.id, otherColumn = r.profileId)
                .select { (r.bookId eq id) and (r.bookCommentId eq c.id) }
                .orderBy(r.id to SortOrder.DESC)
                .mapNotNull { row ->
                    ReplyCommentResponse(
                        row[r.id].value, row[r.comment], row[pf.nickname]
                        , row[r.createdDate], row[c.id].value
                    )
                }
        }
        println(reply+"replyComments ------------------")
        //댓글 찾기
        val comments: List<BookCommentResponse> = transaction {
            (c innerJoin pf leftJoin r)
                .slice(c.columns + c.id + pf.id + pf.nickname + r.columns)
                .select{ (c.bookId eq id) }
                .orderBy(c.id to SortOrder.DESC)
                .mapNotNull { r ->
                    val commentReplies = reply.filter { replyComment ->
                        replyComment.parentId == r[c.id].value
                    }
                    BookCommentResponse (
                        r[c.id].value, r[c.comment], r[pf.nickname],
                        r[c.createdDate], replyComment = commentReplies
                    ) }
        }
        //선호작품 찾기
        val likes = transaction {
            (LikeBooks innerJoin pf)
                .select { LikeBooks.bookId eq id }
                .mapNotNull { r -> LikedBookResponse(
                    r[LikeBooks.id].value, r[pf.id].value, r[LikeBooks.likes]
                ) }
        }
        println("책찾기")
        val book = transaction {
            b.select { (b.id eq id)}
                .mapNotNull { r ->
                    //집계 함수식의 별칭 설정
                    val commentCount = comments.size.toLong()
                    BookResponse(
                        r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                        r[b.description], r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                        r[b.priceStandard], r[b.stockStatus], r[b.cover],
                        r[b.categoryId], r[b.categoryName], commentCount = commentCount,
                        bookComment = comments, likedBook = likes
                    ) }
                .singleOrNull() }

        val itemId = transaction {
            b.select { b.id eq id }.singleOrNull()?.get(b.itemId)?.toInt()
        }
        println(itemId)
        if(itemId != null){
            //조회수 테이블 만들기
            adminService.sendRabbitData(itemId, profileId)
        }

        return book

    }

    //댓글 찾기
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

            //  좋아요를 취소할 때
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

    //신간 도서 댓글 리스트 찾기
//    fun getNewBooksComments(id: Long): List<BookCommentResponse> {
//        val c = BookComments
//        val pf = Profiles
//        val r = ReplyComments
//
//        //답글 찾기
//        val reply : List<ReplyCommentResponse> = transaction {
//            (r innerJoin c).join (pf, JoinType.LEFT, onColumn = pf.id, otherColumn = r.profileId)
//                .select { (r.bookId eq id) and (r.bookCommentId eq c.id) }
//                .orderBy(r.id to SortOrder.DESC)
//                .mapNotNull { row ->
//                    ReplyCommentResponse(
//                        row[r.id].value, row[r.comment], row[pf.nickname]
//                        , row[r.createdDate], row[c.id].value
//                    )
//                }
//        }
//        println(reply+"신간 replyComments ------------------")
//
//        //댓글 찾기
//        val comments: List<BookCommentResponse> = transaction {
//            (c innerJoin pf leftJoin r)
//                .slice(c.columns + c.id + pf.id + pf.nickname + r.columns)
//                    .select{(c.newBookId eq id)}
//                    .orderBy(c.id to SortOrder.DESC)
//                    .mapNotNull { r ->
//                        val commentReplies = reply.filter { replyComment ->
//                            replyComment.parentId == r[c.id].value
//                        }
//                        BookCommentResponse (
//                        r[c.id].value, r[c.comment], r[pf.nickname],
//                        r[c.createdDate], replyComment = commentReplies
//                    ) }
//        }
//        return comments
//    }



}