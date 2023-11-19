package com.example.commerce.sales

import com.example.commerce.books.BookBestResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

// controller - req /  biz-logic  / res
// biz-logic: 연산, 데이터처리, 시스템간 상호작용
// service - biz-logic

@Service
class OrderSalesService(@Qualifier("rabbitTemplate1") private val rabbitTemplate: RabbitTemplate,
                        private val redisTemplate: RedisTemplate<String, String>) {
    private val mapper = jacksonObjectMapper()

//    fun createOrder(orderRequest: OrderSales) {
//        // 주문정보 저장 트랜잭션
//        // ...
//        orderRequest.id = 1
//        for ((index, item) in orderRequest.orderSalesItems.withIndex()) {
//            item.id = (index + 1).toLong()
//        }
//
//        // sales service로 주문정보 전송
//        sendOrder(orderRequest)
//    }

    fun sendOrder(orderRequest: OrderSales) {
        rabbitTemplate.convertAndSend("create-order", mapper.writeValueAsString(orderRequest))
    }

    fun sendMessage(message: String) {
        println("RabbitMQ ==> ex-queue")
        rabbitTemplate.convertAndSend("ex-queue", mapper.writeValueAsString(message))
        println("RabbitMQ ==> ex-queue end.....")
    }

    fun getSalesBestBooks(): List<BookBestResponse> {
        println("--- 레디스에 저장된 베스트셀러 조회  ---")

        val result = redisTemplate.opsForValue().get("sales-best-books")

        println(result)

        return if (result != null) {
            mapper.readValue(result)
        } else {
            println("레디스에 저장된 베스트셀러 조회 실패")
            return listOf()
        }
    }
}