package com.example.commerce.admin

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@FeignClient(name = "office", url="http://192.168.100.94:8082/books")
interface AdminClient {

    @GetMapping("/today")
    fun getTodayBook(@RequestParam("readDate") formattedDateTime: String)
    : TodayDataResponse

}