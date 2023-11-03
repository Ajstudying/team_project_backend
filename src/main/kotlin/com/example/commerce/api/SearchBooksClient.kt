package com.example.commerce.api

import feign.QueryMap
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name="aladin", url="http://www.aladin.co.kr/ttb/api")
interface SearchBooksClient {

    @GetMapping("/ItemSearch.aspx?ttbkey=ttbrkddowls01111124001&Query=마음&QueryType=Keyword&MaxResults=5&start=1&SearchTarget=Book&output=js&Version=20131101")
    fun searchFetch(
    ): NewBookDataResponse

//    @GetMapping("/ItemSearch.aspx")
//    fun searchFetch(@QueryMap params: Map<String, String>): SearchResponse
}