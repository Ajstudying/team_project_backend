package com.example.commerce.books

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.core.io.ResourceLoader
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection

@RestController
@RequestMapping("books")
class BookController (private val resourceLoader: ResourceLoader) {


    @GetMapping
    fun fetch() = transaction {
        Books.selectAll().map{ r -> BookResponse(
            r[Books.id], r[Books.publisher], r[Books.title], r[Books.link], r[Books.author], r[Books.pubDate],
            r[Books.description], r[Books.isbn], r[Books.isbn13], r[Books.itemId], r[Books.priceSales],
            r[Books.priceStandard],  r[Books.stockStatus], r[Books.cover],
            r[Books.categoryId], r[Books.categoryName],
        )}
    }

    // 기본 페이징 조회
    @GetMapping("/paging")
    fun paging(@RequestParam size: Int, @RequestParam page: Int)
    : Page<BookResponse> = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true)
    {
        val b = Books
        val content = Books.selectAll().orderBy(Books.id to SortOrder.DESC)
            .limit(size, offset = (size * page).toLong())
            .map{
                r -> BookResponse(
                    r[b.id], r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                    r[b.description], r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                    r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId], r[b.categoryName]
                )
            }
        val totalCount = Books.selectAll().count()
        return@transaction PageImpl(content, PageRequest.of(page, size), totalCount)
    }

    @GetMapping("/paging/search")
    fun searchPaging(
        @RequestParam size: Int, @RequestParam page: Int, @RequestParam option: String?, @RequestParam keyword: String?
    ):Page<BookResponse> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {
        val b = Books
        val query = when {
            option != null -> {
                // 카테고리를 나누는 동작 처리
                Books.select { b.categoryName like "%${option}%" }
            }
            keyword != null -> {
                // 옵션 검색 동작 처리
                Books.select {
                    (b.title like "%${keyword}%") or
                            (b.categoryName like "%${keyword}%") or
                            (b.author like "%${keyword}%") or
                            (b.publisher like "%${keyword}%")
                }
            }
            else -> {
                // 검색어가 없을시 전체 조회
                b.selectAll()
            }
        }

        val totalCount = query.count()
        val content = query.orderBy(b.id to SortOrder.DESC).limit(size, offset = (size * page).toLong())
            .map{
                    r -> BookResponse(
                r[b.id], r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                r[b.description], r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId], r[b.categoryName]
            )
            }
        return@transaction PageImpl(content, PageRequest.of(page, size), totalCount)
    }

    @GetMapping("/{id}")
    fun selectBook(@PathVariable id: Long): ResponseEntity<BookResponse>{
        val b = Books
        val book = transaction { Books.select { (Books.id eq id)}
            .mapNotNull { r -> BookResponse(
                r[b.id], r[b.publisher], r[b.title], r[b.link], r[b.author], r[b.pubDate],
                r[b.description], r[b.isbn], r[b.isbn13], r[b.itemId], r[b.priceSales],
                r[b.priceStandard], r[b.stockStatus], r[b.cover], r[b.categoryId], r[b.categoryName]
            ) }
            .singleOrNull() }
        return book?.let { ResponseEntity.ok(it) } ?:
        ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }






}