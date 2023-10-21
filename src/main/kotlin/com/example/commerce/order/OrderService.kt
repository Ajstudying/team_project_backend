package com.example.commerce.order

import com.example.commerce.auth.AuthProfile
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(private val database: Database) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)


    // 주문정보 생성
    fun createOrders(orderData: OrderCreateRequest, authProfile: AuthProfile): Long {
        var resultOrderId = 0L;
        transaction {

            try {

                // 1. 주문을 생성하고 orderId 얻기
                val orderId = createOrder(orderData, authProfile)

                println(" -- 주문을 생성하고 orderId 얻기 : " + orderId)

                // 2. 주문 Item(도서)정보 등록
                createOrderItem(orderData, orderId)

                // 3. 배송지 정보 등록
                createOrderAddress(orderData, orderId)

                resultOrderId = orderId
            } catch (e: Exception) {
                rollback()
                //에러메세지 확인
                logger.error(e.message)
                return@transaction 0
            }

        }
        return resultOrderId
    }

    // 주문을 생성하고 orderId 반환
    fun createOrder(req: OrderCreateRequest, authProfile: AuthProfile): Long {
        println("\n<<< OrderService createOrder >>>")
        println(
            "request Data ==> " +
                    ",paymentMethod:" + req.paymentMethod +
                    ",orderStatus:" + req.orderStatus
        )

        // Order.insert를 사용하여 주문 생성
        val orderId = Orders.insert {
            it[Orders.paymentMethod] = req.paymentMethod
            it[Orders.orderStatus] = req.orderStatus
            it[Orders.orderDate] = LocalDateTime.now()
            // 다른 주문 정보 필드들을 채워넣어야 할 수 있음
        } get Orders.id

        println("response Data ==> orderId : " + orderId);

        return orderId // orderId를 반환
    }

    // 주문 Item(도서)정보 등록
    fun createOrderItem(req: OrderCreateRequest, orderId: Long) {
        println("\n<<< OrderService createOrderItem >>>")


        // 주문 항목을 Batch로 처리
        val orderItems = req.orderItems
        OrderItem.batchInsert(orderItems) { orderItem ->
            this[OrderItem.orderId] = orderId
            this[OrderItem.itemId] = orderItem.itemId
            this[OrderItem.quantity] = orderItem.quantity
            this[OrderItem.orderPrice] = orderItem.orderPrice
        }
    }

    // 주문 배송지 정보 생성하고 orderId 반환
    fun createOrderAddress(req: OrderCreateRequest, orderId: Long): Long {
        println("\n<<< OrderService createOrderAddress >>>")
        println(
            "request Data ==> " +
                    ",postcode:" + req.orderAddress.postcode +
                    ",address:" + req.orderAddress.address +
                    ",detailAddress:" + req.orderAddress.detailAddress
        )

        // OrderAddress.insert를 사용하여 주문 배송지 생성
        val orderAddressId = OrderAddress.insert {
            it[OrderAddress.orderId] = orderId
            it[OrderAddress.postcode] = req.orderAddress.postcode
            it[OrderAddress.address] = req.orderAddress.address
            it[OrderAddress.detailAddress] = req.orderAddress.detailAddress
            // 다른 주문 정보 필드들을 채워넣어야 할 수 있음
        } get OrderAddress.id

        println("response Data ==> orderId : " + orderAddressId);

        return orderAddressId // orderId를 반환
    }


}