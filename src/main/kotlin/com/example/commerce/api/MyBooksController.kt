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
@RequestMapping("/api/book-commerce/redis")
class MyBooksController(private val myBooksService: MyBooksService,
                        private val redisTemplate: RedisTemplate<String, String>) {

    //신간 카테고리 검색
    @GetMapping("/category")
    fun searchNewCategory(@RequestParam size:Int, @RequestParam page: Int, @RequestParam option: String?)
            :Page<BookDataResponse> {
        println("신간카테고리조회")
        if(option !== null){
            val result: List<BookDataResponse> = myBooksService.getNewCategory(option)
            //데이터를 페이징 하기 위한 로직
            val start = page * size
            val end = min(start + size.toLong(), result.size.toLong())
            val totalCount = result.size.toLong() // 전체 데이터 개수

            val pagedData = result.subList(start, min(end, totalCount).toInt())

            return PageImpl(pagedData, PageRequest.of(page, size), totalCount)
        }else{
            val result: List<BookDataResponse> = myBooksService.getNewList()
            //데이터를 페이징 하기 위한 로직
            val start = page * size
            val end = min(start + size.toLong(), result.size.toLong())
            val totalCount = result.size.toLong() // 전체 데이터 개수

            val pagedData = result.subList(start, min(end, totalCount).toInt())

            return PageImpl(pagedData, PageRequest.of(page, size), totalCount)
        }

    }

    @GetMapping("/best")
    fun bestFetch()
            :List<BookBestResponse> {
        println("베스트셀러조회")
        val result: List<BookBestResponse> = myBooksService.getBest()

        return result
    }
}