package com.example.commerce.api

import com.example.commerce.books.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/aladin")
class NewBooksApiController (private  val newBooksDataApiService: NewBooksDataApiService){

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    @PostMapping
    fun postDataToMyServer(
            dataFromExternalAPI: List<BookDataResponse>)
    : ResponseEntity<Any> {
        println("신간 db 입력")

        newBooksDataApiService.fetchNewDataToday()
        return ResponseEntity.status(HttpStatus.OK).build()

    }

    @PostMapping("/foreignData")
    fun postForeignDataMyServer(
            foreignDataFromExternalAPI: List<BookDataResponse>)
    : ResponseEntity<Any> {
        println("외국도서신간 db입력")

        newBooksDataApiService.fetchForeignDataToday()
        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @GetMapping("/search")
    fun apiSearchData(
        @RequestParam size: Int, @RequestParam page: Int,
        @RequestParam option: String?, @RequestParam keyword: String)
    : Page<SearchDataResponse> {
        println(option + "조회")
        println(keyword + "조회")

        return transaction {
            val searchTarget = if (option == "외국도서") "Foreign" else "Book"

            val searchResult = newBooksDataApiService.searchApi(keyword, size, page+1, searchTarget)
            if (searchResult != null) {
                val searchResultData = try {
                    val (count, result) = searchResult
                    val totalCount = count.toLong()
                    println("totalCount")
                    PageImpl(result, PageRequest.of(page, size), totalCount)
                } catch (e: Exception) {
                    // 예외 처리: 예외 발생 시 기본 처리 또는 에러 메시지 반환
                    logger.error(e.message + "검색 api 반환 오류")
                    Page.empty()
                }
                return@transaction searchResultData
            } else {
                val b = Books
                val result = b.selectAll().limit(20).map {
                        r -> SearchDataResponse(r[b.title], r[b.link],
                    r[b.author], r[b.pubDate], r[b.description], r[b.isbn], r[b.isbn13],
                        r[b.itemId], r[b.priceSales], r[b.priceStandard], r[b.stockStatus],
                        r[b.cover], r[b.categoryId], r[b.categoryName], r[b.publisher],
                        r[b.customerReviewRank], seriesInfo = null)
                }
                val totalCount = result.count().toLong()
                logger.error("검색 결과가 null입니다.")
                return@transaction  PageImpl(result, PageRequest.of(page, size), totalCount)
            }
        }
    }






}