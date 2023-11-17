package com.example.commerce.payment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment")
class PaymentController(private val paymentService: PaymentService, private val paymentClient: PaymentClient) {

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>
    private val REDIS_KEY = "bank-deposit"

    // Object <-> JSON
    private val mapper = jacksonObjectMapper()

    // 관리자서버로 온라인입금 여부 요청 후 리턴된 결과값을 Redis에 저장한다.
    @GetMapping("/bank-deposit")
    fun setCachedBankDeposit(): ResponseEntity<Any> {
        val result = paymentClient.getBankDeposit()

        println(">> paymentClient.setCachedBankDeposit: " + result)

        // RedisTemplate<key=String, value=String>
        // default: localhost:6379
        redisTemplate.delete(REDIS_KEY) // 캐시 데이터 삭제

        // 캐시 데이터 생성
        redisTemplate.opsForValue()
            .set(REDIS_KEY, mapper.writeValueAsString(result))

        return ResponseEntity.status(HttpStatus.OK).build()
    }

    @GetMapping("/redis")
    fun getCachedBankDeposit(): List<BankDepositResponse> {

        println(">> paymentClient.getBankDeposit: ")

        val REDIS_KEY = "bank-deposit"

        // Object <-> JSON
        val mapper = jacksonObjectMapper()

        val result = redisTemplate.opsForValue().get(REDIS_KEY)
        val bankDepositList: List<BankDepositResponse>

        if (result != null) {
            // value(json) -> List<BankDepositResponse>
            bankDepositList = mapper.readValue(result)
        } else {
            // 빈 리스트 초기화
            bankDepositList = emptyList()
        }

        println(bankDepositList)

        paymentService.updateOrdersStatus(bankDepositList)

        return bankDepositList
    }


    @PostMapping("/bank-deposit-backup")
    fun getCachedBankDeposit_backup(): ResponseEntity<Any> {
        val REDIS_KEY = "bank-deposit"

        // Object <-> JSON
        val mapper = jacksonObjectMapper()

        val result = redisTemplate.opsForValue().get(REDIS_KEY)
        val bankDepositList: List<BankDepositResponse>

        if (result != null) {
            // value(json) -> List<BankDepositResponse>
            bankDepositList = mapper.readValue(result)
        } else {
            // 빈 리스트 초기화
            bankDepositList = emptyList()
        }

        paymentService.updateOrdersStatus(bankDepositList)

        return ResponseEntity.status(HttpStatus.OK).build()
    }


}