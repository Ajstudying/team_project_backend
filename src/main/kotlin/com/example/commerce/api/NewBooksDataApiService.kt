package com.example.commerce.api

import com.example.commerce.books.ForeignBooks
import com.example.commerce.books.NewBooks
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@EnableScheduling
@Component
class NewBooksDataApiService(
        private val newBooksClient: NewBooksClient,
) {
    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    //매월 1일 0시 0분 0초에 실행
//    @Scheduled(cron = "0 0 0 1 * ?")

    //매주 월요일 0시 0분 0초에 실행
//    @Scheduled(cron = "0 0 10 ? * MON")
    //매일 아침 9시 50분
//    @Scheduled(cron = "0 50 9 * * *")
    //매주 월요일 0시 0분 0초에 실행
    @Scheduled(cron = "0 0 10 ? * MON")
    fun fetchNewDataToday() {
        println("신간 스케줄 실행")
        try {
            // NewBooksClient 를 사용하여 데이터를 가져옵니다.
            val dataFromExternalAPI: List<BookDataResponse> = newBooksClient.fetch().item

            //신간도서
            // 가져온 데이터를 처리하고 데이터베이스에 삽입
            transaction {
                // 가져온 데이터를 수정하고 데이터베이스에 삽입
                for (data in dataFromExternalAPI) {
                    // 이미 존재하는 itemId 인지 확인
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

        }catch (e: Exception) {
            //에러메세지 확인
            logger.error(e.message + "신간 오류")
        }

    }

//    @Scheduled(cron = "0 54 17 * * *")
    @Scheduled(cron = "0 0 10 ? * MON")
    fun fetchForeignDataToday() {
        println("외서 스케줄 실행")
        try {
            val foreignDataFromExternalAPI: List<BookDataResponse> = newBooksClient.foreignFetch().item
            //외국도서
            transaction {
                // 가져온 데이터를 수정하고 데이터베이스에 삽입
                for (data in foreignDataFromExternalAPI) {
                    // 이미 존재하는 isbn 인지 확인
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
        }catch (e: Exception) {
        //에러메세지 확인
        logger.error(e.message + "외서 오류")
        }
    }

    fun searchApi(keyword:String, size:Int, page:Int, searchTarget:String )
    : Pair<Int, List<SearchDataResponse>>?{
        try {
            println("$keyword, $size, $page, $searchTarget")
//            val params = mapOf(
//                "ttbkey" to "ttbrkddowls01111124001",
//                "QueryType" to "Title",
//                "output" to "js",
//                "Version" to "20131101",
//                "Query" to "마음",
//                "MaxResults" to "5",
//                "start" to "0",
//                "SearchTarget" to "Book"
//            )
//            val response = searchBooksClient.searchFetch(params)
            val response = newBooksClient.searchFetch(
                    "ttbrkddowls01111124001", keyword, "Keyword", size, page,
                    searchTarget, "js", "20131101")
            val totalResult = response.totalResults
            val result = response.item

            return Pair(totalResult, result)
        } catch (e: Exception) {
            logger.error(e.message + "데이터 받는 거부터 오류")
            return null  // 에러 처리에 따라 null 또는 다른 값을 반환
        }
    }



}