package com.example.commerce.admin

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "office", url="http://192.168.100.177:8082/api")
interface AdminApiClient {

    @GetMapping("/redis-data")
    fun fetchStockStatus(@RequestParam("date") formattedDateTime: String)
    : StockStatusResponse
}