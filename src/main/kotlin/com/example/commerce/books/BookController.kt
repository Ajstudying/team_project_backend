package com.example.commerce.books

import com.example.commerce.api.BookDataResponse
import com.example.commerce.auth.Auth
import com.example.commerce.auth.AuthProfile
import com.example.commerce.auth.Profiles
import com.example.commerce.order.OrderSales
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.coalesce
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
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
import java.lang.Long.min
import java.sql.Connection

@RestController
@RequestMapping("/api/book-commerce/books")
class BookController (private val resourceLoader: ResourceLoader, private val service: BookService) {


    @GetMapping
    fun cacheBooks() :List<BookDataResponse> {
        println("캐시데이터가 실행된다!")
        return service.getCachedBookList()
    }

    //초창기 레디스 연결을 시도할 때 사용했던 book-list 데이터
    //현재 도서몰에는 사용되고 있진 않다.
    @GetMapping("/book-list")
    fun fetch() = transaction() {
        val b = Books
        Books.selectAll().map{ r -> BookDataResponse (
                r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                r[b.description], r[b.isbn], r[b.isbn13],r[b.itemId], r[b.priceSales],
                r[b.priceStandard], r[b.stockStatus], r[b.cover],
                r[b.categoryId], r[b.categoryName], r[b.customerReviewRank],

        )}
    }

    //베스트셀러 목록 레디스 연결
    @GetMapping("/best-list")
    fun getBestList()
            : List<BookBestResponse > = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true){

                println("레디스에 넣을 베스트셀러 목록 조회")
        val b = Books
        val o = OrderSales
        val books =
                b.join(OrderSales, JoinType.INNER, onColumn = o.itemId, otherColumn = b.itemId)
                        .slice(b.columns + o.columns)
                        .selectAll()
                        .groupBy(b.itemId, o.itemId, b.id, o.id)
                        .orderBy(o.bookSales, SortOrder.DESC)
                        .map { r ->
                            //선호작품 찾기
                            val bookId = r[b.id].value
                            val bookLikes = transaction {
                                (b innerJoin LikeBooks)
                                        .select { LikeBooks.bookId eq bookId }
                                        .mapNotNull { r -> LikedBookResponse(
                                                r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
                                        ) }
                            }
                            BookBestResponse (
                                    r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                                    r[b.description],r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                                    r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId],
                                    r[b.categoryName],
                                    r[b.customerReviewRank], likedBook = bookLikes,
                            )
                        }

        val finalBooks = books.ifEmpty {
            Books.selectAll()
                    .map { r ->
                        //선호작품 찾기
                        val bookId = r[b.id].value
                        val bookLikes = transaction {
                            (b innerJoin LikeBooks)
                                    .select { LikeBooks.bookId eq bookId }
                                    .mapNotNull { r ->
                                        LikedBookResponse(
                                                r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
                                        )
                                    }
                        }
                        BookBestResponse(
                                r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                                r[b.description], r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                                r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId],
                                r[b.categoryName],
                                r[b.customerReviewRank], likedBook = bookLikes,
                        )
                    }
        }

        return@transaction finalBooks

    }

    //레디스 연결 전에 사용했던 베스트셀러 db 조회
    //현재는 베스트 셀러 카테고리에서 사용 중이다.
    @GetMapping("/best")
    fun pagingBest(@RequestParam size: Int, @RequestParam page: Int)
    : Page<BookBestResponse > = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true){
        val b = Books
        val o = OrderSales
//        // 주문량 정보를 가져오기 위해 OrderItem 테이블을 조인하고 quantity를 가져옴
//        val quantity =
//            (b leftJoin o)
//                .slice(b.columns + o.columns)
//                .selectAll()
//                .groupBy(b.itemId)
//                .orderBy(OrderItem.quantity.sum(), SortOrder.DESC)

// Books 테이블과 OrderItem 테이블을 조인한 후 주문량 정보를 quantity 필드로 매핑
        val books =
//                (b leftJoin c).join(OrderItem, JoinType.INNER, onColumn = o.itemId, otherColumn = b.itemId)
                //order_item 테이블이 비어있으면 전체 조회하기 위해
            b.join(OrderSales, JoinType.INNER, onColumn = o.itemId, otherColumn = b.itemId)
            .slice(b.columns + o.columns)
            .selectAll()
            .groupBy(b.itemId, o.itemId, b.id, o.id)
            .orderBy(o.bookSales, SortOrder.DESC)
            .limit(size, offset = (size * page).toLong())
            .map { r ->
                //선호작품 찾기
                val bookId = r[b.id].value
                val bookLikes = transaction {
                    (b innerJoin LikeBooks)
                        .select { LikeBooks.bookId eq bookId }
                        .mapNotNull { r -> LikedBookResponse(
                            r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
                        ) }
                }
                BookBestResponse (
                    r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                    r[b.description],r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                    r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId],
                    r[b.categoryName],
                    r[b.customerReviewRank], likedBook = bookLikes,
                )
            }

        val finalBooks = books.ifEmpty {
            Books.selectAll()
                    .limit(size, offset = (size * page).toLong())
                    .map { r ->
                //선호작품 찾기
                val bookId = r[b.id].value
                val bookLikes = transaction {
                    (b innerJoin LikeBooks)
                            .select { LikeBooks.bookId eq bookId }
                            .mapNotNull { r ->
                                LikedBookResponse(
                                        r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
                                )
                            }
                }
                BookBestResponse(
                        r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                        r[b.description], r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                        r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId],
                        r[b.categoryName],
                        r[b.customerReviewRank], likedBook = bookLikes,
                )
            }
        }
        val hasNextPage = min((page + 1) * size.toLong() , finalBooks.size.toLong())

        val pageRequest = PageRequest.of(page, hasNextPage.toInt())
        return@transaction PageImpl(finalBooks, pageRequest, finalBooks.size.toLong())

//        return@transaction PageImpl(books, PageRequest.of(page, size), books.size.toLong())
    }

    //베스트셀러 사이드바 조회
    @GetMapping("/best/category")
    fun pagingBestCategory(@RequestParam size: Int, @RequestParam page: Int,  @RequestParam option: String?)
            : Page<BookBestResponse > = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true){
        val b = Books
        val o = OrderSales

        println("$option 베스트 조회")

        val totalItemCount = transaction(Connection.TRANSACTION_READ_COMMITTED, readOnly = true) {
            b.select { Substring(b.categoryName, intLiteral(1), intLiteral(13)) like "$option%" }
                .count()
        }
        val books =
            b.join(OrderSales, JoinType.LEFT, onColumn = o.itemId, otherColumn = b.itemId)
                .slice(b.columns + o.columns)
                .select { Substring(Books.categoryName, intLiteral(1), intLiteral(13)) like "$option%" }
                .orderBy(coalesce(o.bookSales, intLiteral(0)), SortOrder.DESC)
                .limit(size, offset = (size * page).toLong())
                .map { r ->
                    //선호작품 찾기
                    val bookId = r[b.id].value
                    val bookLikes = transaction {
                        (b innerJoin LikeBooks)
                            .select { LikeBooks.bookId eq bookId }
                            .mapNotNull { r -> LikedBookResponse(
                                r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
                            ) }
                    }
                    BookBestResponse (
                        r[b.id].value, r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                        r[b.description],r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                        r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId],
                        r[b.categoryName],
                        r[b.customerReviewRank], likedBook = bookLikes,
                    )
                }

        return@transaction PageImpl(books, PageRequest.of(page, size), totalItemCount)
    }
    // 신간 조회 / 알라딘에서 받아온 데이터가 저장되어있는 신간도서 테이블에서
    // 북스 테이블로 통합하기 위한 함수
    @GetMapping("/new")
    fun fetchNew(): List<BookDataResponse>
    = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true)
    {
        println("db에 입력하기 위한 신간 조회")
        val n = NewBooks
        val content = NewBooks.selectAll()
            .orderBy(n.id to SortOrder.DESC)
            .map{
                r -> BookDataResponse(
                    r[n.id].value, r[n.publisher], r[n.title], r[n.link], r[n.author], r[n.pubDate],
                    r[n.description], r[n.isbn], r[n.isbn13], r[n.itemId], r[n.priceSales],
                    r[n.priceStandard], r[n.stockStatus], r[n.cover], r[n.categoryId],
                    r[n.categoryName], r[n.customerReviewRank],
                )
            }
        return@transaction content
    }
    // 외서 조회 / 알라딘에서 받아온 데이터가 저장되어있는 외국도서 테이블에서
    // 북스 테이블로 통합하기 위한 함수
    @GetMapping("foreign")
    fun fetchForeign() : List<BookDataResponse> = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true)
    {
        println("db에 입력하기 위한 외국도서 조회")
        val f = ForeignBooks
        val content = ForeignBooks.selectAll()
            .orderBy(f.id to SortOrder.DESC)
            .map{
                    r -> BookDataResponse(
                r[f.id].value, r[f.publisher], r[f.title], r[f.link], r[f.author], r[f.pubDate],
                r[f.description], r[f.isbn], r[f.isbn13], r[f.itemId], r[f.priceSales],
                r[f.priceStandard], r[f.stockStatus], r[f.cover], r[f.categoryId],
                r[f.categoryName], r[f.customerReviewRank],
            )
            }
        return@transaction content
    }

    //레디스에 보내는 자료
    @GetMapping("/new-list")
    fun newListCategory(@RequestParam keyword:String)
    :List<BookDataResponse> = transaction(){
        println("신간카테고리조회 후 레디스 입력")
        println(keyword)
        val n = NewBooks

        //범위지정
        val query = run {
            n.select { Substring(NewBooks.categoryName, intLiteral(1), intLiteral(13)) like "국내도서>$keyword%" }
        }
        println(query)
        val content = query
            .orderBy(n.id to SortOrder.DESC)
            .map { r ->
                BookDataResponse(
                    id = r[n.id].value,
                    publisher = r[n.publisher],
                    title = r[n.title],
                    link = r[n.link],
                    author = r[n.author],
                    pubDate = r[n.pubDate],
                    description = r[n.description],
                    isbn = r[n.isbn],
                    isbn13 = r[n.isbn13],
                    itemId = r[n.itemId],
                    priceSales = r[n.priceSales],
                    priceStandard = r[n.priceStandard],
                    stockStatus = r[n.stockStatus],
                    cover = r[n.cover],
                    categoryId = r[n.categoryId],
                    categoryName = r[n.categoryName],
                    customerReviewRank = r[n.customerReviewRank],
                )
            }
        return@transaction content
    }

    //카테고리 검색
    @GetMapping("/category")
    fun searchCategory(
        @RequestParam size: Int, @RequestParam page: Int,
//        @RequestParam new: Int?,
        @RequestParam option: String?,
    ):Page<BookListResponse> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true){

//        if(new == 0) {
//            println("신간카테고리조회")
//            val n = NewBooks
//            val c = BookComments
//
//            //집계 함수식의 별칭 설정
//            val commentCount = service.getCommentCount()
//
//            val query = when {
//                option != null -> {
//                    (n leftJoin c)
//                        .slice(n.columns + commentCount)
//                        .select { Substring(NewBooks.categoryName, intLiteral(1), intLiteral(13)) like "$option%" }
//                        .groupBy(n.id)
//
//
//                }else -> {
//                    // 검색어가 없을시 전체 조회
//                    NewBooks.selectAll()
//                }
//            }
//
//            //전체 결과 카운트
//            val totalCount = query.count()
//            val content = query
//                .orderBy(n.id to SortOrder.DESC)
//                .limit(size, offset = (size * page).toLong())
//                .map{
//                        r ->
//                    //선호작품 찾기
//                    val bookId = r[n.id].value
//                    val bookLikes = transaction {
//                        (n innerJoin LikeBooks)
//                            .select { LikeBooks.newBookId eq bookId}
//                            .mapNotNull { r -> LikedBookResponse(
//                                r[LikeBooks.id].value, r[LikeBooks.profileId].value, r[LikeBooks.likes]
//                            ) }
//                    }
//                    BookListResponse(
//                    r[n.id].value, r[n.publisher], r[n.title], r[n.link], r[n.author], r[n.pubDate],
//                    r[n.description], r[n.isbn], r[n.isbn13],r[n.itemId], r[n.priceSales],
//                    r[n.priceStandard], r[n.stockStatus], r[n.cover],
//                    r[n.categoryId], r[n.categoryName], r[n.customerReviewRank],
//                        r[commentCount], likedBook = bookLikes,
//                ) }
//            return@transaction PageImpl(content, PageRequest.of(page, size), totalCount)
//
//        }else {
            println("도서카테고리조회")
            val b = Books
            val c = BookComments

            //집계 함수식의 별칭 설정
            val commentCount = c.id.count().alias("commentCount")

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
                        r[b.categoryId], r[b.categoryName], r[b.customerReviewRank],
                        r[commentCount], likedBook = bookLikes,
                    ) }

            return@transaction PageImpl( content, PageRequest.of(page, size), totalCount )
//        }
    }

    //검색 / 알라딘 검색 api 연결 전에 사용했던 검색 기능.
    // "해리 포터" 로 저장되어 있는 db 조회 검색으로는 "해리포터"로 검색했을 때 제대로 안나오기 때문에
    // 클라이언트 입장에서 불편할 것으로 사료돼 알라딘 검색 api 연결로 수정 변경함
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
        val commentCount = service.getCommentCount()

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
                                ((b.title.trim() like "%${keyword}%") or
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
                    (b.title.trim() like "%${keyword}%") or
                            (b.categoryName like "%${keyword}%") or
                            (b.author like "%${keyword}%") or
                            (b.publisher like "%${keyword}%")}
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
            .orderBy(b.isbn to SortOrder.DESC)
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
                r[b.customerReviewRank],r[commentCount], likedBook = bookLikes,
            ) }
        return@transaction PageImpl(content, PageRequest.of(page, size), totalCount)
    }

    //도서상세
    @GetMapping("/{id}")
    fun selectBook(
        @PathVariable id: Long)
    : ResponseEntity<BookResponse>{
        println("도세 상세페이지 조회")

        val book = service.getBooks(id)

        return book?.let { ResponseEntity.ok(it) } ?:
        ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    //알라딘 api 로 검색된 도서들로도 상세페이지를 보고 싶어하는 클라이언트가 있을 것이라 생각돼
    // 알라딘 api로 들어오는 itemId로 우리 도서몰 쪽 상세페이지 확인이 가능하도록 함.
    // not found 라는 응답이 나가면 프론트에서 해결하도록 처리
    @GetMapping("/itemId")
    fun selectSearchBook(
            @RequestParam itemId: Int)
            : ResponseEntity<BookResponse>{
        println("검색도서 상세페이지 조회")

        val b = Books
        val c = BookComments
        val pf = Profiles
        val r = ReplyComments
        val selectedBookId = transaction {
            val selectedBook = b.select { b.itemId eq itemId }.singleOrNull()
            val bookId = selectedBook?.get(b.id)?.value
            return@transaction bookId
        }
        //답글 찾기
        val reply : List<ReplyCommentResponse> = transaction {
            (r innerJoin c).join (pf, JoinType.LEFT, onColumn = pf.id, otherColumn = r.profileId)
                    .select { (r.bookId eq selectedBookId) and (r.bookCommentId eq c.id) }
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
                    .select{ (c.bookId eq selectedBookId) }
                    .orderBy(c.id to SortOrder.DESC)
                    .mapNotNull { r ->
                        val commentReplies = reply.filter { replyComment ->
                            replyComment.parentId == r[c.id].value
                        }
                        BookCommentResponse (
                                r[c.id].value, r[c.comment], r[pf.nickname],
                                r[c.createdTime], replyComment = commentReplies
                        ) }
        }
        //선호작품 찾기
        val likes = transaction {
            (LikeBooks innerJoin pf)
                    .select { LikeBooks.bookId eq selectedBookId }
                    .mapNotNull { r -> LikedBookResponse(
                            r[LikeBooks.id].value, r[pf.id].value, r[LikeBooks.likes]
                    ) }
        }
        println("책찾기")
        val book = transaction {
            b.select { (b.id eq selectedBookId)}
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
    fun selectNewBook(
        @PathVariable id: Long)
    : ResponseEntity<BookResponse>{
        val book = service.getNewBook(id)

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
                    it[createdTime] = createCommentRequest.createdDate
                    if (createCommentRequest.new == 0) {
                        it[newBookId] = id
                    } else {
                        it[bookId] = id
                    }
                }.resultedValues
                    ?: return@transaction ResponseEntity.status(HttpStatus.BAD_REQUEST)

                val record = response.first()
                return@transaction SaveBookCommentResponse(
                        record[BookComments.id].value ,record[BookComments.comment],
                        authProfile.nickname, record[BookComments.createdTime])
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

    //선호작품 표시
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
    //알림설정표시
    @Auth
    @PutMapping("/{itemId}/alam")
    fun toggleAlam
                (@PathVariable itemId: Int,
                 @RequestBody request: CreateAlamRequest,
                 @RequestAttribute authProfile: AuthProfile) {
        println("알림 설정 등록 시작")
        service.updateAlamRecord(itemId, authProfile.id, request.alamDisplay)

    }

    //댓글에 답글
    @Auth
    @PostMapping("/reply/{pageId}/{commentId}")
    fun createReplyComment
            (@PathVariable pageId: Long, @PathVariable commentId: Long,
             @RequestBody createCommentRequest: CreateCommentRequest,
             @RequestAttribute authProfile: AuthProfile)
            : ResponseEntity<Any> {
        println("${createCommentRequest.new} 신간의 답글인가")
        println(createCommentRequest.comment)

        if(!createCommentRequest.validate()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

        val (result, response) = transaction {
            //원 댓글이 있는지 확인
            BookComments.select{ BookComments.id eq commentId}.singleOrNull()
                    ?: return@transaction Pair(false, null)

            //있으면 답글 추가
            val response = ReplyComments.insert {
                it[comment] = createCommentRequest.comment
                it[profileId] = authProfile.id
                it[createdDate] = createCommentRequest.createdDate
                if (createCommentRequest.new == 0) {
                    it[newBookId] = pageId
                } else {
                    it[bookId] = pageId
                }
                it[bookCommentId] = commentId
            }.resultedValues ?: return@transaction Pair(false, null)

            val record = response.first()
            return@transaction Pair(true, ReplyCommentResponse(record[ReplyComments.id].value,
                    record[ReplyComments.comment],
                    authProfile.nickname,
                    record[ReplyComments.createdDate], record[ReplyComments.bookCommentId].value))
        }
        if(result){
            return ResponseEntity.status(HttpStatus.CREATED).body(response)
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

    }

    //답글 삭제
    @Auth
    @DeleteMapping("/reply/{commentId}/{id}")
    fun deleteReplyComment(@PathVariable commentId: Long, @PathVariable id: Long,
                           @RequestAttribute authProfile: AuthProfile): ResponseEntity<Any>{

        //댓글 찾기
        transaction {
            ReplyComments.select(where =
            (ReplyComments.id eq id) and
                    (ReplyComments.profileId eq authProfile.id) and
                    (ReplyComments.bookCommentId eq commentId)).firstOrNull()
        }?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        //댓글 삭제
        transaction {
            ReplyComments.deleteWhere { ReplyComments.id eq id }
        }
        //200 OK
        return ResponseEntity.ok().build()
    }

    //신간 도서 재고가 입고됐을 때 업데이트 된 알림 테이블 조회 함수
    //알림 테이블 업데이트 쪽에 같이 둘까 하다가 프론트에 내보내는 데이터들을 같이 모아 두는 게 나을 것 같아서
    //북스 컨트롤러에 같이 둠.
    @Auth
    @GetMapping("/alam")
    fun getAlamData(@RequestAttribute authProfile: AuthProfile): List<AlamBookResponse>{
        println("알림 설정 북 조회")
        val (result, findedAlam) = transaction {
            val findedAlam =
                AlamBooks.join(Books, JoinType.INNER, onColumn = AlamBooks.bookItemId, otherColumn = Books.itemId)
                .select { AlamBooks.profileId eq authProfile.id }
                .mapNotNull { r -> AlamBookResponse(
                    r[AlamBooks.bookItemId], authProfile.id, r[AlamBooks.alamDisplay],
                    r[AlamBooks.alam], r[Books.title],
                ) }
            return@transaction Pair(true, findedAlam)
        }

        if(result){
            return findedAlam
        }else{
            return listOf()
        }
    }






}