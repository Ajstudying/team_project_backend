package com.example.commerce.books


import com.example.commerce.auth.AuthProfile
import com.example.commerce.auth.Profiles
import com.example.commerce.inventory.HitsTable
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.sql.Connection

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
    fun getBooksData() : List<BookDataResponse>{
        return transaction {
            Books.selectAll()
                .orderBy(Books.id to SortOrder.DESC)
                .map { r ->
                BookDataResponse(
                    id = r[Books.id].value,
                    publisher = r[Books.publisher],
                    title = r[Books.title],
                    link = r[Books.link],
                    author = r[Books.author],
                    pubDate = r[Books.pubDate],
                    description = r[Books.description],
                    isbn = r[Books.isbn],
                    isbn13 = r[Books.isbn13],
                    itemId = r[Books.itemId],
                    priceSales = r[Books.priceSales],
                    priceStandard = r[Books.priceStandard],
                    stockStatus = r[Books.stockStatus],
                    cover = r[Books.cover],
                    categoryId = r[Books.categoryId],
                    categoryName = r[Books.categoryName],
                    customerReviewRank = r[Books.customerReviewRank],
                )
            }
        }
    }
    @GetMapping("new")
    fun newBooksFetch(): List<BookDataResponse>
    @GetMapping("foreign")
    fun newForeignFetch(): List<BookDataResponse>
    //카테고리 검색
    @GetMapping("/new-list")
    fun searchNewCategory(@RequestParam keyword: String): List<BookDataResponse>


}
@Service
class BookService
    (private val myBooksClient: MyBooksClient,
     private val rabbitTemplate: RabbitTemplate,
     private val redisTemplate: RedisTemplate<String, String>)
//     private val redisTemplate: RedisTemplate<Long, String>)
{

    private val mapper = jacksonObjectMapper()

    @Scheduled(cron = "0 0 10 * * *")
//    @Scheduled(cron = "0 0 0 1 * ?")
    fun scheduledFetchBooksData() {
        println("--- booksData fetching ---")
        val items = myBooksClient.getBooksData()

    val keywords = arrayOf(
        "소설/시/희곡", "사회과학", "에세이", "여행", "역사", "예술/대중문화", "어린이", "외국어",
        "요리/살림", "유아", "인문학", "자기계발", "종교/역학",
        "과학", "경제경영", "건강/취미", "만화",
    )

        keywords.forEach { keyword ->
            redisTemplate.delete(keyword)
            val category = myBooksClient.searchNewCategory(keyword) // 키워드에 해당하는 카테고리 이름을 가져오는 함수
            println(category)
            redisTemplate.opsForValue().set(keyword, mapper.writeValueAsString(category))
        }
        //결과값 저장
        redisTemplate.delete("book-list")
        redisTemplate.opsForValue().set("book-list", mapper.writeValueAsString(items))

//        items.forEach{ data ->
//            // 책 데이터를 JSON 형태로 변환
//
//            val key = "book:${data.id}" // 각 도서에 대한 고유한 키 생성
//            val bookJson = mapper.writeValueAsString(data)
//            redisTemplate.opsForValue().set(key, bookJson) // 해당 키에 도서 정보 저장
//        }
    }

    //매주 월요일 실행
//    @Scheduled(cron = "0 31 17 * * *")
    @Scheduled(cron = "0 10 10 ? * MON")
    fun scheduledNewBooks() {
        println("신간도서 원래 도서목록에 추가 스케줄 실행")
        //신간 도서 등록
        setNewBooks(myBooksClient.newBooksFetch())
    }

//    @Scheduled(cron = "0 33 17 * * *")
    @Scheduled(cron = "0 12 10 ? * MON")
    fun scheduledForeignBooks() {
        println("외국도서 원래 도서목록에 추가 스케줄 실행")
        //신간 도서 등록
        setForeignBooks(myBooksClient.newForeignFetch())
    }

    fun getNewCategory(option: String): List<BookDataResponse> {
        val result = redisTemplate.opsForValue().get(option)
        if(result != null) {
            return mapper.readValue(result)
        }else{
            return listOf()
        }
    }
    fun setNewBooks(dataList:List<BookDataResponse>){
        println("신간도서 원래 도서목록에 추가해요!!")
        transaction {
            // 가져온 데이터를 수정하고 데이터베이스에 삽입
            for (data in dataList) {
                // 이미 존재하는 itemId인지 확인
                val existingBook = Books.select { Books.itemId eq data.itemId }.singleOrNull()

                if (existingBook == null) {
                    Books.insert {
                        it[this.publisher] = data.publisher
                        it[this.title] = data.title
                        it[this.link] = data.link
                        it[this.author] = data.author
                        it[this.pubDate] = data.pubDate
                        it[this.description] = data.description
                        it[this.isbn] = data.isbn
                        it[this.isbn13] = data.isbn13
                        it[this.itemId] = data.itemId
                        it[this.priceSales] = data.priceSales
                        it[this.priceStandard] = data.priceStandard
                        it[this.stockStatus] = data.stockStatus
                        it[this.cover]=data.cover
                        it[this.categoryId] = data.categoryId
                        it[this.categoryName] = data.categoryName
                        it[this.customerReviewRank] = data.customerReviewRank

                    }.resultedValues ?: return@transaction null

                }

            }

        }

    }
    fun setForeignBooks(dataList:List<BookDataResponse>){
        println("외국도서 원래 도서목록에 추가해요!!")
        transaction {
            // 가져온 데이터를 수정하고 데이터베이스에 삽입
            for (data in dataList) {
                // 이미 존재하는 itemId인지 확인
                val existingBook = Books.select { Books.itemId eq data.itemId }.singleOrNull()

                if (existingBook == null) {
                    Books.insert {
                        it[this.publisher] = data.publisher
                        it[this.title] = data.title
                        it[this.link] = data.link
                        it[this.author] = data.author
                        it[this.pubDate] = data.pubDate
                        it[this.description] = data.description
                        it[this.isbn] = data.isbn
                        it[this.isbn13] = data.isbn13
                        it[this.itemId] = data.itemId
                        it[this.priceSales] = data.priceSales
                        it[this.priceStandard] = data.priceStandard
                        it[this.stockStatus] = data.stockStatus
                        it[this.cover]=data.cover
                        it[this.categoryId] = data.categoryId
                        it[this.categoryName] = data.categoryName
                        it[this.customerReviewRank] = data.customerReviewRank

                    }.resultedValues ?: return@transaction null

                }

            }

        }

    }

    fun getCachedBookList(): List<BookDataResponse> {
        val result = redisTemplate.opsForValue().get("book-list")
        if(result != null){
            return mapper.readValue(result)
        }else{
            return listOf()
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

    //카운트별칭
    fun getCommentCount(): ExpressionAlias<Long> {
        val c = BookComments
        return c.id.count().alias("commentCount")
    }

    //신간 도서 상세 찾기
    fun getNewBook(id: Long, authProfile: AuthProfile?): BookResponse? {
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

        //조회수 테이블 만들기
        val newHits = transaction {
            val hit = HitsTable.insert {
                it[this.itemId] = id.toInt()
                it[this.hitsCount] = 1
                if (authProfile != null) {
                    it[this.profileId] = authProfile.id
                }
            }.resultedValues ?: return@transaction null
            return@transaction hit
        }
        //조회수 row객체보내기
        if(newHits != null){
            println("신간데이터 레빗으로 보내요")
            sendHits(newHits)
        }

        return newBook
    }
    //조회수 레빗mq
    fun sendHits(hits: List<ResultRow>){
        println("이제 진짜 레빗으로 가요")
        rabbitTemplate.convertAndSend("hits-queue", mapper.writeValueAsString(hits))
    }

    //도서 상세 조회
    fun getBooks (id: Long, authProfile: AuthProfile?) : BookResponse? {
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

        //조회수 테이블 만들기
        val newHits = transaction {
            val hit = HitsTable.insert {
                it[this.itemId] = id.toInt()
                it[this.hitsCount] = 1
                if (authProfile != null) {
                    it[this.profileId] = authProfile.id
                }
            }.resultedValues ?: return@transaction null
            return@transaction hit
        }
        //조회수 row객체보내기
        if(newHits != null){
            println("도서데이터 레빗으로 보내요")
            sendHits(newHits)
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