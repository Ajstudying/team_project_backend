package com.example.commerce.order

import com.example.commerce.auth.AuthProfile
import com.example.commerce.books.Books
import com.example.commerce.cart.Cart
import com.example.commerce.cart.CartItem
import com.example.commerce.sales.OrderSalesService
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
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
        var c = Cart

        // 장바구니 id 얻기
        // 주문생성 후 삭제할 장바구니 id를 먼저 얻어놓기(등록하는 트랙잰션 범위안에 포함시키지 않으려고 미리 조회)
        val cartRecord = transaction {
            Cart.slice(c.id)
                .select { Cart.profileId eq authProfile.id }.singleOrNull()
        }

        val cartId = cartRecord?.get(c.id)

        if (cartId == null) {
            println(
                "cartId is null. Check the cart table with authProfile : "
                        + authProfile.id + "," + authProfile.userid
            )
            return 0;
        }

        var resultOrderId = 0L;
        transaction {
            try {

                // 1. 주문을 생성하고 orderId 얻기
                val orderId = createOrder(orderData, authProfile)

                println(" -- 주문을 생성하고 orderId 얻기 : " + orderId)

                // 2. 주문 Item(도서)정보 등록

                val resultOrderItems = createOrderItem(orderData, orderId)

                // 3. 배송지 정보 등록
                createOrderAddress(orderData, orderId)

                // 4. cart, cart_item 정보 삭제
                if (cartId != null) {
                    deleteCart(cartId.toLong())
                };

                // 5. 주문 판매데이터 업데이트
                updateOrderSales(resultOrderItems)

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
                    ",paymentPrice:" + req.paymentPrice +
                    ",orderStatus:" + req.orderStatus
        )

        // Order.insert를 사용하여 주문 생성
        val orderId = Orders.insert {
            it[Orders.paymentMethod] = req.paymentMethod
            it[Orders.paymentPrice] = req.paymentPrice
            it[Orders.orderStatus] = req.orderStatus
            it[Orders.orderDate] = LocalDateTime.now()
            it[Orders.profileId] = authProfile.id
            // 다른 주문 정보 필드들을 채워넣어야 할 수 있음
        } get Orders.id

        println("response Data ==> orderId : " + orderId);

        return orderId // orderId를 반환
    }

    // 주문 Item(도서)정보 등록
    fun createOrderItem(req: OrderCreateRequest, orderId: Long): List<OrderSalesRequest> {
        println("\n<<< OrderService createOrderItem >>>")


        // 주문 항목을 Batch로 처리
        val orderItems = req.orderItems
        OrderItem.batchInsert(orderItems) { orderItem ->
            this[OrderItem.orderId] = orderId
            this[OrderItem.itemId] = orderItem.itemId
            this[OrderItem.quantity] = orderItem.quantity
            this[OrderItem.orderPrice] = orderItem.orderPrice
        }

        // 주문 항목 요청을 OrderItem 객체 배열로 변환
        val resultOrderItems = req.orderItems.map { orderItemRequest ->
            OrderSalesRequest(
                itemId = orderItemRequest.itemId,
                quantity = orderItemRequest.quantity,
            )
        }

        return resultOrderItems;
    }

    // 주문 배송지 정보 생성하고 orderId 반환
    fun createOrderAddress(req: OrderCreateRequest, orderId: Long): Long {
        println("\n<<< OrderService createOrderAddress >>>")
        println(
            "request Data ==> " +
                    ",deliveryName:" + req.orderAddress.deliveryName +
                    ",deliveryPhone:" + req.orderAddress.deliveryPhone +
                    ",postcode:" + req.orderAddress.postcode +
                    ",address:" + req.orderAddress.address +
                    ",detailAddress:" + req.orderAddress.detailAddress +
                    ",deliveryMemo:" + req.orderAddress.deliveryMemo

        )

        // OrderAddress.insert를 사용하여 주문 배송지 생성
        val orderAddressId = OrderAddress.insert {
            it[OrderAddress.orderId] = orderId
            it[OrderAddress.deliveryName] = req.orderAddress.deliveryName
            it[OrderAddress.deliveryPhone] = req.orderAddress.deliveryPhone
            it[OrderAddress.postcode] = req.orderAddress.postcode
            it[OrderAddress.address] = req.orderAddress.address
            it[OrderAddress.detailAddress] = req.orderAddress.detailAddress
            it[OrderAddress.deliveryMemo] = req.orderAddress.deliveryMemo
            // 다른 주문 정보 필드들을 채워넣어야 할 수 있음
        } get OrderAddress.id

        println("response Data ==> orderId : " + orderAddressId);

        return orderAddressId // orderId를 반환
    }

    // 주문 판매수량 업데이트
    fun updateOrderSales(orderItemStock: List<OrderSalesRequest>) {
        println("\n<<< OrderService createOrderAddress >>>")
        println("request Data (주문 항목) ==> ")
        println("주문 항목:")

        for (reqItem in orderItemStock) {
            println("도서 ID: ${reqItem.itemId}, 수량: ${reqItem.quantity}")

            // select id from order_stock where item_id = 1;
            val query = OrderSales.select {
                (OrderSales.itemId eq reqItem.itemId)
            }

            if (query.count() > 0) {
                // 이미 해당 도서의 판매정보가 등록되어 있으면 기존 판매수에 수량을 더한다
                // SQL
                // UPDATE order_stock SET book_stock = book_stock + 2 WHERE item_id = 1;
                OrderSales.update({ OrderSales.itemId eq reqItem.itemId }) {
                    it[bookSales] = OrderSales.bookSales + reqItem.quantity.toInt()
                }
            } else {
                // 신규이면 새로 등록한다.
                // SQL
                // select id from books where item_id = :itemId;
                val bookId = Books.select {
                    (Books.itemId eq reqItem.itemId)
                }.first()
                    .let { it[Books.id] }

                // INSERT INTO order_stock( book_stock, item_id, status, book_id)
                // values (?, ?, ?,  (select id from books where item_id =?))
                OrderSales.insert {
                    it[OrderSales.bookSales] = reqItem.quantity
                    it[OrderSales.itemId] = reqItem.itemId
                    it[OrderSales.status] = "1"
                    it[OrderSales.bookId] = bookId

                } get OrderSales.id
            }

        }
    }

    // 주문생성 후 장바구니 내역을 삭제한다.
    fun deleteCart(cartId: Long) {
        println("\n<<< OrderService deleteCart >>>")
        println(
            "request Data ==> " +
                    ",cartId:" + cartId
        )

        // delete FROM cart_item where cart_id = ?
        CartItem.deleteWhere { CartItem.cartId eq cartId }

        // delete FROM cart where id = 1;
        Cart.deleteWhere { Cart.id eq cartId }

//        return
    }


}