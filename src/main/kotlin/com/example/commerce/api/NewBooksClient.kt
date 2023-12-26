package com.example.commerce.api

import feign.QueryMap
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

// 외부 알라딘 오픈 API에서 데이터를 가져옴
@FeignClient(name="newBook", url="http://www.aladin.co.kr/ttb/api")
interface NewBooksClient {
    @GetMapping("/ItemList.aspx?ttbkey=ttbrkddowls01111124001&QueryType=ItemNewAll&MaxResults=50&start=1&SearchTarget=Book&output=js&Version=20131101")
    fun fetch() : NewBookDataResponse

    @GetMapping("/ItemList.aspx?ttbkey=ttbrkddowls01111124001&QueryType=ItemNewSpecial&MaxResults=100&start=1&SearchTarget=Foreign&output=js&Version=20131101")
    fun foreignFetch(): NewBookDataResponse

    //검색 파라미터 정확한 연결이 어려웠다.
    // 원래는 변수 이름을 param이름으로 맞춰서 했다가
    // 각각 명시적으로 지정해주는 방법으로 했다.
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

}

