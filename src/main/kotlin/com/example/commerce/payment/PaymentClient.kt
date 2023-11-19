package com.example.commerce.payment

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping


@FeignClient(name = "paymentClient", url = "http://192.168.0.5:8082/payment")
//@FeignClient(name = "paymentClient", url = "http://192.168.100.204:8082/payment")
interface PaymentClient {
    @GetMapping("/bank-deposit")
    fun getBankDeposit(): List<BankDepositResponse>
}