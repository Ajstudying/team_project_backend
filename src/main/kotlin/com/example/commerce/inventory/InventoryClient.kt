package com.example.commerce.inventory

import com.example.commerce.books.BookDataResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.PathVariable
//
////다른 서비스(서버)에 HTTP 요청을 보내는 클라이언트를 작성
////@FeignClient(name = "inventoryClient", url = "http://192.168.100.36:8082 /inventories")
//interface InventoryClient {
//    @GetMapping("/{productId}")
//    fun fetchProductStocks(@PathVariable productId: Int): Int?
//}

