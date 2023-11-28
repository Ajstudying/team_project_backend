package com.example.commerce.admin

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@FeignClient(name = "office")
interface AdminClient {

    @GetMapping("/today")
    fun getTodayBook(@RequestParam("readDate") formattedDateTime: String)
    : TodayDataResponse

    @GetMapping("/with-file")
    fun downloadFile(): List<MainFileResponse>

}