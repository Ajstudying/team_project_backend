package com.example.commerce.product

import com.example.commerce.books.BookDataResponse
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@EnableScheduling
@Component
class NewBooksDataApiService(
        private val newBooksApiController: NewBooksApiController,
        private val newBooksClient: NewBooksClient
) {
    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    //매월 1일 0시 0분 0초에 실행
//    @Scheduled(cron = "0 0 0 1 * ?")

    //매주 월요일 0시 0분 0초에 실행
//    @Scheduled(cron = "0 0 10 ? * MON")
    //매일 아침 9시 50분
    @Scheduled(cron = "0 50 9 * * *")
    fun fetchDataMonthly() {
        println("스케줄 실행")
        try {
            // NewBooksClient를 사용하여 데이터를 가져옵니다.
            val dataFromExternalAPI: List<BookDataResponse> = newBooksClient.fetch().item
            val foreignDataFromExternalAPI: List<BookDataResponse> = newBooksClient.foreignFetch().item

            // 가져온 데이터를 처리하고 데이터베이스에 삽입
            newBooksApiController.postDataToMyServer(dataFromExternalAPI)
            newBooksApiController.postForeignDataMyServer(foreignDataFromExternalAPI)
        }catch (e: Exception) {
            //에러메세지 확인
            logger.error(e.message)
        }

    }



}