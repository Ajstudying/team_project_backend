package com.example.commerce.admin

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

//백오피스와 재고 업데이트 연결하는 api
@FeignClient(name = "api-office")
interface AdminApiClient {

    @GetMapping("/redis-data")
    fun fetchStockStatus(@RequestParam("date") formattedDateTime: String)
    : List<StockStatusResponse>
}