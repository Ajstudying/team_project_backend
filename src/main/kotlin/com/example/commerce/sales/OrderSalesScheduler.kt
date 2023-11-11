package com.example.commerce.sales

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@EnableScheduling
@Component
class OrderSalesScheduler(
        private val orderSalesController: OrderSalesController,
        private val orderSalesService: OrderSalesService
) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    // Object <-> JSON
    private val mapper = jacksonObjectMapper()


    // 주문데이터의 판매정보를 관리시스템으로 전송
    // 처리 간격 : 5분
    @Scheduled(cron = "0 */1 * * * *")
    fun scheduledSSendOrderSales() {
        println("======= 주문데이터의 판매정보를 관리시스템으로 전송(5분 간격)  ${Date().time} =======")

        try {
            // 주문 송신 배치처리가 안된 주문정보 조회(판매정보 미처리 건)
            // return type : List<OrderSalesItem>
            val orderList = orderSalesController.fetchOrderDataForBatch()

            println("*** 주문정보 조회(판매정보 미처리 건) 건수 : " + orderList.size)

            for (reqItems in orderList) {
                println("*** 주문정보 조회(판매정보 미처리 건) 상세내용 : *** $reqItems")
                val orderItemList = orderSalesController.fetchOrderItemsDataForBatch(reqItems.id)

                println(orderItemList)

                // 주문정보 담기(관리시스템으로 주문정보 RabbitMQ로 전송)
                val orderRequest = OrderSales(
                        id = reqItems.id,
                        name = reqItems.name,
                        address = reqItems.address,
                        orderSalesItems = orderItemList
                )

                println(orderRequest)

                // 관리시스템으로 주문정보 RabbitMQ로 전송
                orderSalesService.sendOrder(orderRequest);

                // 배치 처리 업데이트
                orderSalesController.modifyOrderBatchStatus(reqItems.id)
            }

        } catch (e: Exception) {
            //에러메세지 확인
            logger.error(e.message)
        }

    }


}