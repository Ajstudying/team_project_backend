package com.example.commerce.sales

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders/sales")
class OrderSalesController(private val orderService: OrderSalesService) {

    @PostMapping
    fun createOrder(@RequestBody orderRequest: OrderSales) {
        // 요청값 검증

        orderService.sendOrder(orderRequest)

        // 응답값 반환
    }

    @PostMapping("/send-message")
    fun sendMessage(@RequestBody message: String) {
        // 요청값 검증

        orderService.sendMessage(message)

        // 응답값 반환
    }
}