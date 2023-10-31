package com.example.commerce.payment

data class BankDepositResponse(
    val orderId: Long,      // 주문 id
    val deposit: String     // 무통장 입금여부
)