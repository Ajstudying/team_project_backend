package com.example.commerce.api

import feign.QueryMap
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

// 외부 API에서 데이터를 가져옴
@FeignClient(name="newBook", url="http://www.aladin.co.kr/ttb/api")
interface NewBooksClient {
    @GetMapping("/ItemList.aspx?ttbkey=ttbrkddowls01111124001&QueryType=ItemNewAll&MaxResults=10&start=1&SearchTarget=Book&output=js&Version=20131101")
    fun fetch() : NewBookDataResponse

    @GetMapping("/ItemList.aspx?ttbkey=ttbrkddowls01111124001&QueryType=ItemNewAll&MaxResults=10&start=1&SearchTarget=Foreign&output=js&Version=20131101")
    fun foreignFetch(): NewBookDataResponse

}

