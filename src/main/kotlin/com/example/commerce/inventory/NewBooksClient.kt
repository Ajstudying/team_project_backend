package com.example.commerce.inventory

import com.example.commerce.books.NewBookDataResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

// 외부 API에서 데이터를 가져옴
@FeignClient(name="ItemNewAll", url="http://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=ttbrkddowls01111124001&QueryType=ItemNewAll&MaxResults=10&start=1&SearchTarget=Book&output=js&Version=20131101")
interface NewBooksClient {
    @GetMapping
    fun fetch() : NewBookDataResponse
}

