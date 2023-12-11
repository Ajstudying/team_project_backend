package com.example.commerce.api

import com.example.commerce.books.BookBestResponse
import com.example.commerce.books.Books
import com.example.commerce.books.NewBooks
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@EnableScheduling
@Component
class MyBooksService(
        private val myBooksClient: MyBooksClient,
        private val redisTemplate: RedisTemplate<String, String>) {

    private val mapper = jacksonObjectMapper()


//    @Scheduled(cron = "0 20 14 * * *")
//@Scheduled(cron = "0 */30 * * * *")
    @Scheduled(cron = "0 */10 * ? * MON")
    fun scheduledFetchBooksData() {
        println("--- newBookData fetching ---")
        val items = myBooksClient.newBooksFetch()
        val keywords = arrayOf(
                "소설/시/희곡", "사회과학", "에세이", "여행", "역사", "예술/대중문화", "어린이", "외국어",
                "요리/살림", "유아", "인문학", "자기계발", "종교/역학",
                "과학", "경제경영", "건강/취미", "만화",
        )

        keywords.forEach { keyword ->
            redisTemplate.delete(keyword)
            val category = myBooksClient.searchNewCategory(keyword) // 키워드에 해당하는 카테고리 이름을 가져오는 함수
            println(category)
            redisTemplate.opsForValue().set(keyword, mapper.writeValueAsString(category))
        }
        //결과값 저장
        redisTemplate.delete("new-list")
        redisTemplate.opsForValue().set("new-list", mapper.writeValueAsString(items))

//        items.forEach{ data ->
//            // 책 데이터를 JSON 형태로 변환
//
//            val key = "book:${data.id}" // 각 도서에 대한 고유한 키 생성
//            val bookJson = mapper.writeValueAsString(data)
//            redisTemplate.opsForValue().set(key, bookJson) // 해당 키에 도서 정보 저장
//        }
    }

//    @Scheduled(cron = "0 20 14 * * *")
//    @Scheduled(cron = "0 */30 * * * *")
    @Scheduled(cron = "0 */10 * ? * MON")
    fun scheduledFetchBestBooksData() {
        println("--- bestData fetching ---")
        val items = myBooksClient.bestFetch()

        //결과값 저장
        redisTemplate.delete("best-list")
        redisTemplate.opsForValue().set("best-list", mapper.writeValueAsString(items))

    }

    //매주 월요일 실행
//    @Scheduled(cron = "0 31 17 * * *")
    @Scheduled(cron = "0 1 10 ? * MON")
    fun scheduledNewBooks() {
        println("신간도서 원래 도서목록에 추가 스케줄 실행")
        //신간 도서 등록
        setNewBooks(myBooksClient.newBooksFetch())
    }
//    @Scheduled(cron = "0 57 17 * * *")
    @Scheduled(cron = "0 1 10 ? * MON")
    fun scheduledForeignBooks() {
        println("외국도서 원래 도서목록에 추가 스케줄 실행")
        //신간 도서 등록
        setForeignBooks(myBooksClient.newForeignFetch())
    }

    fun setNewBooks(dataList:List<BookDataResponse>){
        println("신간도서 원래 도서목록에 추가해요!!")
        transaction {
            // 가져온 데이터를 수정하고 데이터베이스에 삽입
            for (data in dataList) {
                // 이미 존재하는 itemId 인지 확인
                val existingBook = Books.select { Books.itemId eq data.itemId }.singleOrNull()

                if (existingBook == null) {
                    Books.insert {
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

    }
    fun setForeignBooks(dataList:List<BookDataResponse>){
        println("외국도서 원래 도서목록에 추가해요!!")
        transaction {
            // 가져온 데이터를 수정하고 데이터베이스에 삽입
            for (data in dataList) {
                // 이미 존재하는 itemId 인지 확인
                val existingBook = Books.select { Books.itemId eq data.itemId }.singleOrNull()

                if (existingBook == null) {
                    Books.insert {
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

    }

    //신간 카테고리 레디스에서 검색
    fun getNewCategory(option: String ): List<BookDataResponse> {
        println("신간 카테고리 레디스에서 조회해오기")
        val result = redisTemplate.opsForValue().get(option)
        return if(result != null) {
            mapper.readValue(result)
        }else{
            println("레디스 조회 실패")
            return listOf()
        }
    }
    fun getNewList(): List<BookDataResponse> {
        println("신간 레디스에서 조회해오기")
        val result = redisTemplate.opsForValue().get("new-list")
        return if(result != null) {
            mapper.readValue(result)
        }else{
            println("레디스 조회 실패")
            return listOf()
        }
    }

    //레디스에서 베스트셀러 조회
    fun getBest(): List<BookBestResponse> {
        println("레디스에서 베셀 조회해오기")
        val result = redisTemplate.opsForValue().get("best-list")
        return if(result != null) {
            mapper.readValue(result)
        }else{
            println("레디스 조회 실패")
            return listOf()
        }
    }


}