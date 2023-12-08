package com.example.commerce.payment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@EnableScheduling
@Component

class BankDataCheckScheduler(
        private val paymentService: PaymentService,
        private val paymentClient: PaymentClient,
        private val redisTemplate: RedisTemplate<String, String>
) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)
    private val REDIS_KEY = "bank-deposit"

    // Object <-> JSON
    private val mapper = jacksonObjectMapper()


    // 1시간 마다 처리
    @Scheduled(cron = "0 0 1 * * *")
    fun scheduledFetchBankDeposit() {
        println("======= scheduledFetchBankDeposit (간격:1시간) ${Date().time} =======행")

        try {
            val result = paymentClient.getBankDeposit()

            println(">> 배치서버로 온라인입금 여부에 대한 요청 처리: " + result)

//            // RedisTemplate<key=String, value=String>
//            // default: localhost:6379
//            redisTemplate.delete(REDIS_KEY) // 캐시 데이터 삭제
//            // 캐시 데이터 생성
//            redisTemplate.opsForValue()
//                    .set(REDIS_KEY, mapper.writeValueAsString(result))

            paymentService.updateOrdersStatus(result);

        } catch (e: Exception) {
            //에러메세지 확인
            logger.error(e.message)
        }

    }

    fun getCachedBankDeposit(): List<BankDepositResponse> {

        val result = redisTemplate.opsForValue().get(REDIS_KEY)
        if (result != null) {
            // value(json) -> List<TopProductResponse>
            val list: List<BankDepositResponse> = mapper.readValue(result)
            return list
        } else {
            // 빈배열 반환
            return listOf()
        }
    }
}