package com.example.commerce.api

import com.example.commerce.books.BookBestResponse
import com.example.commerce.books.NewBooks
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.Long
import java.lang.Long.min
import java.sql.Connection


@RestController
@RequestMapping("/redis")
class MyBooksController(private val myBooksService: MyBooksService,
                        private val redisTemplate: RedisTemplate<String, String>) {

    @GetMapping("/new")
    fun fetchRedisNew(@RequestParam size:Int, @RequestParam page:Int)
            : Page<BookDataResponse>
            = transaction (Connection.TRANSACTION_READ_COMMITTED, readOnly = true)
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
        val totalCount = n.selectAll().count()
        return@transaction PageImpl( content, PageRequest.of(page, size), totalCount )
    }

    //신간 카테고리 검색
    @GetMapping("/category")
    fun searchNewCategory(@RequestParam size:Int, @RequestParam page: Int, @RequestParam option: String)
            :Page<BookDataResponse> {
        println("신간카테고리조회")
        val result: List<BookDataResponse> = myBooksService.getNewCategory(option)
        //데이터를 페이징 하기 위한 로직
        val start = page * size
        val end = min(start + size.toLong(), result.size.toLong())
        val totalCount = result.size.toLong() // 전체 데이터 개수

        val pagedData = result.subList(start, min(end, totalCount).toInt())

        return PageImpl(pagedData, PageRequest.of(page, size), totalCount)
    }

    @GetMapping("/best")
    fun bestFetch()
            :List<BookBestResponse> {
        println("베스트셀러조회")
        val result: List<BookBestResponse> = myBooksService.getBest()

        return result
    }
}