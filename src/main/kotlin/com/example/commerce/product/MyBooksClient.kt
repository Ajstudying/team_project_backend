package com.example.commerce.product

import com.example.commerce.books.BookDataResponse
import com.example.commerce.books.Books
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

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