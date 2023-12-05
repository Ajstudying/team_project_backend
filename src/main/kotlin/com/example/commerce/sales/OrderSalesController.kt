package com.example.commerce.sales

import com.example.commerce.auth.Auth
import com.example.commerce.books.BookBestResponse
import com.example.commerce.books.Books
import com.example.commerce.order.OrderAddress
import com.example.commerce.order.OrderItem
import com.example.commerce.order.Orders
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.web.bind.annotation.*
import java.sql.Connection

@RestController
@RequestMapping("/api/order-commerce/orders/sales")
class OrderSalesController(private val orderService: OrderSalesService) {

    @PostMapping
    fun createOrder(@RequestBody orderRequest: OrderSales) {
        // 요청값 검증

        orderService.sendOrder(orderRequest)

        // 응답값 반환
    }

    @PostMapping("/send-message")
    fun sendMessage(@RequestBody message: String) {
        // 요청값 검증

        orderService.sendMessage(message)

        // 응답값 반환
    }

    // 주문 송신 배치처리가 안된 주문정보 조회(판매정보 미처리 건)
    @GetMapping("/orders/to-order-master")
    fun fetchOrderDataForBatch():
            List<OrderMasterForSales> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {

        println("<<< OrderSalesController fetchOrderDataForBatch >>>")

        // SQL
        val orderInfo =
                (Orders innerJoin OrderAddress)
                        .slice(Orders.id, OrderAddress.deliveryName, OrderAddress.address)
                        .select { (OrderAddress.orderId eq Orders.id) and (Orders.batchStatus eq null) and (Orders.paymentMethod neq "3") }
                        .map {
                            OrderMasterForSales(
                                    it[Orders.id],
                                    it[OrderAddress.deliveryName],
                                    it[OrderAddress.address]
                            )
                        }

        return@transaction orderInfo
    }

    @GetMapping("/orders/to-order-items")
    fun fetchOrderItemsDataForBatch(orderId: Long):
            List<OrderSalesItem> = transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {

        println("<<< OrderSalesController fetchOrderItemsDataForBatch >>>")

        // SQL
//        select orders.id, order_item.item_id, order_item.quantity, order_item.order_price from orders
//        inner join order_item where orders.id = order_item.order_id
//        and orders.batch_status != "1";
        val orderSalesItem =
                (Orders innerJoin OrderItem)
                        .join(Books, JoinType.INNER, onColumn = OrderItem.itemId, otherColumn = Books.itemId)
                        .slice(Orders.id, OrderItem.itemId, Books.title, OrderItem.quantity, OrderItem.orderPrice)
                        .select { (Orders.id eq orderId) and (OrderItem.orderId eq Orders.id) }
                        .map {
                            OrderSalesItem(
                                    it[Orders.id],
                                    it[OrderItem.itemId].toLong(),
                                    it[Books.title],
                                    it[OrderItem.quantity],
                                    it[OrderItem.orderPrice].toLong(),
                            )
                        }

        return@transaction orderSalesItem
    }

    // 주문정보 송신 완료 처리
    @Auth
    @PutMapping("/detail/to-order-done")
    fun modifyOrderBatchStatus(orderId: Long) {

        println("<<< OrderSalesController modifyOrderBatchStatus >>>")

        val o = Orders;

        // id에 해당 레코드가 없으면 return
        transaction {
            o.select { (o.id eq orderId) }.firstOrNull()
        } ?: return;

        // 송신 완료 처리 : batch-status("1")로 업데이트 처리
        transaction {
            o.update({ o.id eq orderId }) {
                it[batchStatus] = "1";
            }
        }
    }

    @GetMapping("/best-books")
    fun getSalesBestBooks()
            : List<BookBestResponse> {
        println("베스트셀러조회")
        val result: List<BookBestResponse> = orderService.getSalesBestBooks()

        return result
    }
}