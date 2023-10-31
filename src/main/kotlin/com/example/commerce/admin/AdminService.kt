package com.example.commerce.admin

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@EnableScheduling
@Component
class AdminService(
        private val adminClient: AdminClient,
        private val adminController: AdminController) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

//    @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(cron = "0 54 14 * * *")
    fun fetchTodayData() {
        println("오늘의 북데이터 가져오기")
        val currentDateTime = LocalDateTime.now()
        // 출력 형식을 정의하기 위한 DateTimeFormatter 사용 (선택 사항)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDateTime = currentDateTime.format(formatter)

        try {
            println("데이터 받기 시작해요!!")
            val dataAPI: TodayDataResponse = adminClient.getTodayBook("2023-10-31")

            adminController.todayDataToMyServer(dataAPI)
        }catch (e: Exception){
        logger.error(e.message)
        }
    }

}