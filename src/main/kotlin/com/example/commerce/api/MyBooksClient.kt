package com.example.commerce.api

import com.example.commerce.books.BookBestResponse
import com.example.commerce.books.BookListResponse
import com.example.commerce.books.Books
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

//레디스에 넣을 데이터를 아래와 같은 형식으로 만듬
//혹시나 다른 쪽에서 주는 데이터를 레디스로 넣게 될 수도 있을 거 같아서 만들었으나
//현재는 내 db 데이터로 내보내고 있음.
@FeignClient(name="books")
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

    @GetMapping("/best-list")
    fun bestFetch(): List<BookBestResponse>

}