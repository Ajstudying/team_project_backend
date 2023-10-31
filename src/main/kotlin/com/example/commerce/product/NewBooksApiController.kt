package com.example.commerce.product

import com.example.commerce.books.BookDataResponse
import com.example.commerce.books.ForeignBooks
import com.example.commerce.books.NewBooks
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/endpoint")
class NewBooksApiController{

    @PostMapping
    fun postDataToMyServer(
            dataFromExternalAPI: List<BookDataResponse>)
    : ResponseEntity<Any> {
        println("신간 db 입력")

        transaction {
            // 가져온 데이터를 수정하고 데이터베이스에 삽입
            for (data in dataFromExternalAPI) {
                // 이미 존재하는 itemId인지 확인
                val existingBook = NewBooks.select { NewBooks.itemId eq data.itemId }.singleOrNull()

                if (existingBook == null) {
                    NewBooks.insert {
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
        return ResponseEntity.status(HttpStatus.OK).build()

    }

    @PostMapping("/foreignData")
    fun postForeignDataMyServer(
            foreignDataFromExternalAPI: List<BookDataResponse>)
    : ResponseEntity<Any> {
        println("외국도서신간 db입력")

        transaction {
            // 가져온 데이터를 수정하고 데이터베이스에 삽입
            for (data in foreignDataFromExternalAPI) {
                // 이미 존재하는 isbn인지 확인
                val existingBook = ForeignBooks.select { ForeignBooks.isbn eq data.isbn }.singleOrNull()

                if (existingBook == null) {
                    ForeignBooks.insert {
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
        return ResponseEntity.status(HttpStatus.OK).build()
    }






}