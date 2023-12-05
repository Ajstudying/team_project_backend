package com.example.commerce.admin

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "api-office")
interface AdminApiClient {

    @GetMapping("/redis-data")
    fun fetchStockStatus(@RequestParam("date") formattedDateTime: String)
    : List<StockStatusResponse>
}