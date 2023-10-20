package com.example.commerce.inventory

import com.example.commerce.books.BookDataResponse
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@EnableScheduling
@Component
class NewBookDataScheduler(
    private val newBookApiController: NewBookApiController, private val newBooksClient: NewBooksClient
) {
    //매월 1일 0시 0분 0초에 실행
//    @Scheduled(cron = "0 0 0 1 * ?")

    //매주 월요일 0시 0분 0초에 실행
    @Scheduled(cron = "0 0 10 ? * MON")
    fun fetchDataMonthly() {
        // NewBooksClient를 사용하여 데이터를 가져옵니다.
        val dataFromExternalAPI: List<BookDataResponse> = newBooksClient.fetch().item

        // 가져온 데이터를 처리하고 데이터베이스에 삽입
        newBookApiController.postDataToMyServer()
    }

}