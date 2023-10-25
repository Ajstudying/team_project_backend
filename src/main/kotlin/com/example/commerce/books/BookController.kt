package com.example.commerce.books

import com.example.commerce.auth.Auth
import com.example.commerce.auth.AuthProfile
import com.example.commerce.auth.Profiles
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.time.LocalDateTime

@RestController
@RequestMapping("books")
class BookController (private val resourceLoader: ResourceLoader, private val service: BookService) {


    @GetMapping
    fun fetch() = transaction() {
        val b = Books
        Books.selectAll().map{ r -> BookDataResponse (
                r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                r[b.description], r[b.isbn], r[b.isbn13],r[b.itemId], r[b.priceSales],
                r[b.priceStandard], r[b.stockStatus], r[b.cover],
                r[b.categoryId], r[b.categoryName], r[b.customerReviewRank],

        )}
    }
    @GetMapping("/best")
    fun pagingBest(@RequestParam size: Int, @RequestParam page: Int)
    : Page<BookBestResponse> = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true){
        val b = Books
//        val c = Carts

        //조인 및 특정 칼럼선택 count 함수 사용
        val slices =
//            (b innerJoin c)
            b.slice(b.columns) //c.sales_amt

        Books.selectAll()
//            .orderBy(Cart.sales_amt to SortOrder.DESC)
            .map{ r -> BookBestResponse(
            r[Books.id].value, r[Books.publisher], r[Books.title], r[Books.link], r[Books.author], r[Books.pubDate],
            r[Books.description], r[Books.itemId], r[Books.priceSales],
            r[Books.priceStandard],  r[Books.stockStatus], r[Books.cover],
            r[Books.categoryId], r[Books.categoryName],r[Books.customerReviewRank],
        )}
        return@transaction PageImpl(listOf())
    }

    // 신간 조회
    @GetMapping("/new")
    fun pagingNew(@RequestParam size: Int, @RequestParam page: Int)
    : Page<BookDataResponse> = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true)
    {
        println("신간 조회")
        val n = NewBooks
        val content = NewBooks.selectAll()
            .orderBy(n.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong())
            .map{
                r -> BookDataResponse(
                    r[n.id].value, r[n.publisher], r[n.title], r[n.link], r[n.author], r[n.pubDate],
                    r[n.description], r[n.isbn], r[n.isbn13], r[n.itemId], r[n.priceSales],
                    r[n.priceStandard], r[n.stockStatus], r[n.cover], r[n.categoryId],
                    r[n.categoryName], r[n.customerReviewRank],
                )
            }
        val totalCount = Books.selectAll().count()
        return@transaction PageImpl(content, PageRequest.of(page, size), totalCount)
    }

    //카테고리 검색
    @GetMapping("/category")
    fun searchCategory(
        @RequestParam size: Int, @RequestParam page: Int, @RequestParam new: Int?, @RequestParam option: String?,
    ):Page<BookListResponse> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true){

        if(new == 0) {
            println("신간카테고리조회")
            val n = NewBooks
            val c = BookComments

            //집계 함수식의 별칭 설정
            val commentCount = service.getComment()

            val query = when {
                option != null -> {
                    (n leftJoin c)
                        .slice(n.columns + LikeBooks.columns + commentCount)
                        .select { Substring(NewBooks.categoryName, intLiteral(1), intLiteral(13)) like "$option%" }
                        .groupBy(n.id)


                }else -> {
                    // 검색어가 없을시 전체 조회
                    NewBooks.selectAll()
                }
            }

            //전체 결과 카운트
            val totalCount = query.count()
            val content = query
                .orderBy(n.id to SortOrder.DESC)
                .limit(size, offset = (size * page).toLong())
                .map{
                        r ->
                    //선호작품 찾기
                    val bookId = r[n.id].value
                    val bookLikes = transaction {
                        (n innerJoin LikeBooks)
                            .select { LikeBooks.newBookId eq bookId}
                            .mapNotNull { r -> LikedBookResponse(
                                r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
                            ) }
                    }
                    BookListResponse(
                    r[n.id].value, r[n.publisher], r[n.title], r[n.link], r[n.author], r[n.pubDate],
                    r[n.description], r[n.isbn], r[n.isbn13],r[n.itemId], r[n.priceSales],
                    r[n.priceStandard], r[n.stockStatus], r[n.cover],
                    r[n.categoryId], r[n.categoryName], r[n.customerReviewRank],r[commentCount], likeBooks = bookLikes,
                ) }
            return@transaction PageImpl(content, PageRequest.of(page, size), totalCount)

        }else {
            println("도서카테고리조회")
            val b = Books
            val c = BookComments

            //집계 함수식의 별칭 설정
            val commentCount = service.getComment()

            val query = when {
                option != null -> {
                    (b leftJoin c)
                        .slice(b.columns + commentCount)
                        .select { Substring(Books.categoryName, intLiteral(1), intLiteral(13)) like "$option%" }
                        .groupBy(b.id)
                }else -> {
                    // 검색어가 없을시 전체 조회
                    Books.selectAll()
                }
            }

            //전체 결과 카운트
            val totalCount = query.count()
            val content = query
                .orderBy(b.id to SortOrder.DESC)
                .limit(size, offset = (size * page).toLong())
                .map{
                        r ->
                    val bookId = r[b.id].value
                    val bookLikes = transaction {
                        (b innerJoin LikeBooks)
                            .select { LikeBooks.bookId eq bookId }
                            .mapNotNull { r -> LikedBookResponse(
                                r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
                            ) }
                    }
                    BookListResponse(
                    r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                    r[b.description], r[b.isbn], r[b.isbn13],r[b.itemId], r[b.priceSales],
                    r[b.priceStandard], r[b.stockStatus], r[b.cover],
                    r[b.categoryId], r[b.categoryName], r[b.customerReviewRank],r[commentCount], likeBooks = bookLikes,
                ) }
            return@transaction PageImpl(content, PageRequest.of(page, size), totalCount)
        }
    }

    //검색
    @GetMapping("/paging/search")
    fun searchPaging(
        @RequestParam size: Int, @RequestParam page: Int, @RequestParam option: String?, @RequestParam keyword: String?
    ):Page<BookListResponse> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        println(option + "조회")
        println(keyword + "조회")

        //단축 이름 변수 사용
        val b = Books
        val c = BookComments

        //집계 함수식의 별칭 설정
        val commentCount = service.getComment()

//        val commonQuery = (b innerJoin c)
//            .slice(b.id, b.publisher, b.title, b.link, b.author, b.pubDate,
//                b.description, b.itemId, b.priceSales,
//                b.priceStandard,b.stockStatus,b.cover,b.categoryId,b.categoryName,
//                commentCount)
//            .groupBy(b.id)

        //검색 조건 설정
        val query = when {

            option != null && keyword != null -> {
                (b leftJoin c)
                    .slice(b.columns + commentCount)
                    .select {
                        (Substring(b.categoryName, intLiteral(1), intLiteral(4)) like "$option%") and
                                ((b.title like "%${keyword}%") or
                                        (b.categoryName like "%${keyword}%") or
                                        (b.author like "%${keyword}%") or
                                        (b.publisher like "%${keyword}%")) }
                    .groupBy(b.id)
            }

            option != null -> {
                // 카테고리를 나누는 동작 처리
                (b leftJoin c)
                    .slice(b.columns + commentCount)
                    .select { Substring(b.categoryName, intLiteral(1), intLiteral(4)) like "$option%" }
                    .groupBy(b.id)
            }
            keyword != null -> {
                // 옵션 검색 동작 처리
                (b leftJoin c)
                    .slice(b.columns + commentCount)
                    .select {
                    (b.title like "%${keyword}%") or
                            (b.categoryName like "%${keyword}%") or
                            (b.author like "%${keyword}%") or
                            (b.publisher like "%${keyword}%") or
                            (b.title like "%${keyword}%") }
                    .groupBy(b.id)
            }
            else -> {
                // 검색어가 없을시 전체 조회
                b.selectAll().groupBy(b.id)
            }
        }
        //전체 결과 카운트
        val totalCount = query.count()
        val content = query
            .orderBy(b.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong())
            .map{
                    r ->
                //선호작품 찾기
                val bookId = r[b.id].value
                val bookLikes = transaction {
                    (b innerJoin LikeBooks)
                        .select { LikeBooks.bookId eq bookId }
                        .mapNotNull { r -> LikedBookResponse(
                            r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
                        ) }
                }
                BookListResponse(
                r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                r[b.description],r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId],
                r[b.categoryName],
                r[b.customerReviewRank],r[commentCount], likeBooks = bookLikes,
            ) }
        return@transaction PageImpl(content, PageRequest.of(page, size), totalCount)
    }

    //도서상세
    @GetMapping("/{id}")
    fun selectBook(@PathVariable id: Long): ResponseEntity<BookResponse>{
        println("도서상세페이지조회")
        val b = Books
        val c = BookComments
        val pf = Profiles

//        val slices = (b leftJoin (c innerJoin pf))
//                .slice(b.columns + c.id + commentCount + pf.id)
        println("댓글찾기")
        //댓글 찾기
        val comments: List<BookCommentResponse> = transaction {
            (c innerJoin pf)
                .select{(c.bookId eq id)}
                .orderBy(c.id to SortOrder.DESC)
                .mapNotNull { r -> BookCommentResponse (
                    r[c.id].value, r[c.comment], r[pf.nickname], r[c.createdDate],
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
        return book?.let { ResponseEntity.ok(it) } ?:
        ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    //신간도서상세
    @GetMapping("/new/{id}")
    fun selectNewBook(@PathVariable id: Long): ResponseEntity<BookResponse>{
        println("신간상세페이지조회")
        val comments: List<BookCommentResponse> = service.getNewBooksComments(id)

        //선호작품 찾기
        val likes = transaction {
            (LikeBooks innerJoin Profiles).select { LikeBooks.newBookId eq id }.mapNotNull { r -> LikedBookResponse(
                r[LikeBooks.id].value,r [Profiles.id].value, r[LikeBooks.likes],
            ) }
        }
        val book = service.getNewBook(id, comments, likes)

        return book?.let { ResponseEntity.ok(it) } ?:
        ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    //댓글 추가
    @Auth
    @PostMapping("/{id}")
    fun createComment
            (@PathVariable id: Long,
             @RequestBody createCommentRequest: CreateCommentRequest,
             @RequestAttribute authProfile: AuthProfile)
    : ResponseEntity<Any> {
        println("${createCommentRequest.new} 신간인가")
        println(createCommentRequest.comment)

        if(!createCommentRequest.validate()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
        val result = try {
            transaction {
                val response = BookComments.insert {
                    it[comment] = createCommentRequest.comment
                    it[profileId] = authProfile.id
                    it[createdDate] = createCommentRequest.createdDate
                    if (createCommentRequest.new == 0) {
                        it[newBookId] = id
                    } else {
                        it[bookId] = id
                    }
                }.resultedValues
                    ?: return@transaction ResponseEntity.status(HttpStatus.BAD_REQUEST)

                val record = response.first()
                return@transaction BookCommentResponse(record[BookComments.id].value ,record[BookComments.comment], authProfile.nickname, record[BookComments.createdDate])
            }
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.")
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }
    //댓글 수정
    @Auth
    @PutMapping("/{id}")
    fun modifyComment
            (@PathVariable id: Long,
             @RequestBody request: BookCommentModifyRequest ,
             @RequestAttribute authProfile: AuthProfile)
    : ResponseEntity<Any> {
        println("댓글 수정")

        //객체값 널체크
        if(request.comment.isNullOrEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("message" to "content are required"))
        }
        //댓글 찾기
        service.findComment(id, authProfile.id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        //댓글 수정
        transaction {
            BookComments.update( { BookComments.id eq id } ) {
                if(!request.comment.isNullOrEmpty()){
                    it[comment] = request.comment
                }
            }
        }

        return ResponseEntity.ok().build();
    }
    //댓글 삭제
    @Auth
    @DeleteMapping("/{id}")
    fun removeComment(@PathVariable id: Long,
                      @RequestAttribute authProfile: AuthProfile): ResponseEntity<Any>{
        //댓글 찾기
        service.findComment(id, authProfile.id)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        //댓글 삭제
        transaction {
            BookComments.deleteWhere { BookComments.id eq id }
        }
        //200 OK
        return ResponseEntity.ok().build()
    }

    @Auth
    @PutMapping("/{id}/like")
    fun toggleLike
                (@PathVariable id: Long,
                 @RequestBody request: CreateLikeRequest,
                 @RequestAttribute authProfile: AuthProfile)
    :ResponseEntity<Any>{

        return if (request.new == 0) {
            service.updateLikeRecord(id, authProfile.id, request.like, request.new)
        } else {
            service.updateLikeRecord(id, authProfile.id, request.like, null)
        }
    }






}