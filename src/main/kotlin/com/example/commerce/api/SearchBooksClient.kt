package com.example.commerce.api

import feign.QueryMap
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name="aladin", url="http://www.aladin.co.kr/ttb/api")
interface SearchBooksClient {

    @GetMapping("/ItemSearch.aspx")
    fun searchFetch(
            @RequestParam("ttbkey") ttbkey:String,
            @RequestParam("Query") keyword:String,
            @RequestParam("QueryType") QueryType:String,
            @RequestParam("MaxResults") size: Int,
            @RequestParam("start") page: Int,
            @RequestParam("SearchTarget") searchTarget: String,
            @RequestParam("output") output: String,
            @RequestParam("Version") Version: String,
    ): SearchResponse

//    @GetMapping("/ItemSearch.aspx")
//    fun searchFetch(@QueryMap params: Map<String, String>): SearchResponse
}