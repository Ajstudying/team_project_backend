package com.example.commerce.payment

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment")
class PaymentController(private val paymentService: PaymentService, private val paymentClient: PaymentClient) {

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @PostMapping("/bank-deposit")
    fun getCachedBankDeposit(): ResponseEntity<Any> {
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