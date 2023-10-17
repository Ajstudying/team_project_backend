package com.example.commerce.inventory

import com.example.commerce.books.BookResponse
import com.example.commerce.books.NewBookResponse
import com.example.commerce.books.NewBooks
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/endpoint")
class OpenApiController {
    private val newBooksClient: NewBooksClient

    @Autowired
    constructor(newBooksClient: NewBooksClient) {
        this.newBooksClient = newBooksClient
    }

    @PostMapping
    fun postDataToMyServer(): ResponseEntity<Any> {

        val dataFromExternalAPI: List<NewBookResponse> = newBooksClient.fetch().item


        val result = transaction {
            // 가져온 데이터를 수정하고 데이터베이스에 삽입
            for (data in dataFromExternalAPI) {
                val newBook = NewBooks.insert {
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
                    it[this.cover] = data.cover
                    it[this.categoryId] = data.categoryId
                    it[this.categoryName] = data.categoryName

                }.resultedValues
                    ?: return@transaction null

            }

        }
        return ResponseEntity.status(HttpStatus.OK).build()

    }


}