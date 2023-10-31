package com.example.commerce.payment

import com.example.commerce.order.Orders
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping

@Service
class PaymentService(private val database: Database) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    // 무통장 입금여부가 Y로 들어오면 주문완료로 처리한다.
    fun updateOrdersStatus(bankDepositList: List<BankDepositResponse>) {
        println("\n<<< PaymentService 무통장 입금 후 주문완료 처리 >>>")

        transaction {
            for (depositList in bankDepositList) {
                println("depositList => orderId: ${depositList.orderId}, 입금여부: ${depositList.deposit}")

                // select * from orders WHERE id = :id
                val query =
                    Orders.select {
                        (Orders.id eq depositList.orderId)
                    }

                if (query.count() > 0) {
                    // 해당 주문건의 무통장 입금여부가 Y로 들어와서 주문완료로 처리한다.
                    // SQL
                    // UPDATE orders SET order_status = 1 WHERE id = :id

                    Orders.update({ Orders.id eq depositList.orderId }) {
                        it[orderStatus] = "1";
                    }
                }
            }
        }

    }


}


