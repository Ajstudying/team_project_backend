package com.example.commerce.order

import com.example.commerce.auth.Auth
import com.example.commerce.auth.AuthProfile
import com.example.commerce.books.Books
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.*
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Connection
import java.sql.Date

//@Tag(name="주문 처리 API")
@RestController
@RequestMapping("/order")
class OrderController(private val service: OrderService) {

    // 주문 생성
    @Auth
    @PostMapping(value = ["/add"])
    fun create(@RequestBody request: OrderCreateRequest, @RequestAttribute authProfile: AuthProfile):
            ResponseEntity<Long> {

        println("<<< OrderController /order/add >>>")
        println("입력 값 확인")
        println(
            "profileId:" + authProfile.userid.toString() +
                    ",paymentMethod:" + request.paymentMethod +
                    ",paymentPrice:" + request.paymentPrice +
                    ",orderStatus:" + request.orderStatus
        )

        for (bookItem in request.orderItems) {
            "itemId:" + bookItem.itemId +
                    ",quantity:" + bookItem.quantity +
                    ",orderPrice:" + bookItem.orderPrice
        }

        println(
            ",postcode:" + request.orderAddress.postcode +
                    ",address:" + request.orderAddress.address +
                    ",detailAddress:" + request.orderAddress.detailAddress
        )


        // 필요한 request 값이 빈값이면 400 : Bad request
        if (!request.validate()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400)
        }

        // 주문 등록 후 orderId return
        val orderId = service.createOrders(request, authProfile)

        return if (orderId > 0) {
            println(
                "주문하기 성공 : 주문번호 : " + orderId
            );

            ResponseEntity.status(HttpStatus.CREATED).body(orderId)
        } else {
            println(
                "주문하기 실패 : 주문번호 : "
            );

            ResponseEntity.status(HttpStatus.CONFLICT).body(orderId)
        }
    }

    // 주문 목록 조회
    @Auth
    @GetMapping("/paging")
    fun paging(
        @RequestParam size: Int, @RequestParam page: Int,
        @RequestParam startDate: String, @RequestParam endDate: String,
        @RequestAttribute authProfile: AuthProfile
    ): Page<OrderResponse> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {

            println("<<< OrderController /order/list >>>")
            println("입력 값 확인")
            println(
                "size:" + size +
                        ",page:" + page +
                        ",startDate:" + startDate +
                        ",endDate:" + endDate
            )

            val o = Orders // table alias

            // 로그인 유저의 주문내역 select
            // 주문일자로 기간 조회
            val query = Orders
                .slice(o.id, o.paymentMethod, o.paymentPrice, o.orderStatus, o.orderDate)
                .select {
                    (Orders.profileId eq authProfile.id) and
                            (Date(Orders.orderDate) greaterEq Date.valueOf(startDate)) and
                            (Date(Orders.orderDate) lessEq Date.valueOf(endDate))
                }

            // 해당 주문건의 대표되는 도서 1개 조회를 위해 별도 function 처리함
            val result = transaction {
                query
                    .orderBy(Orders.id to SortOrder.DESC)
                    .limit(size, offset = (size * page).toLong())
                    .map { r ->
                        val orderId = r[Orders.id]
                        val booksInfo = getBooksInfoForOrder(orderId)

                        OrderResponse(
                            r[Orders.id],
                            r[Orders.paymentMethod],
                            r[Orders.paymentPrice],
                            r[Orders.orderStatus],
                            r[Orders.orderDate].toString(),
                            booksInfo
                        )
                    }
            }

            // 전체 결과 카운트
            val totalCount = query.count()

            return@transaction PageImpl(
                result, // List<OrderResponse>(컬렉션)
                PageRequest.of(page, size), // Pageable
                totalCount // 전체 건수
            )
        }

    // 해당 주문건의 대표되는 도서 1개 조회를 위해 별도 function 처리함
    fun getBooksInfoForOrder(orderId: Long): OrderItemResponse2 {
        val booksInfo =
            (Books)
                .join(OrderItem, JoinType.INNER, onColumn = OrderItem.itemId, otherColumn = Books.itemId)
                .slice(Books.itemId, Books.title, Books.cover)
                .select { OrderItem.orderId eq orderId }
                .map {
                    OrderItemResponse2(
                        orderId,
                        it[Books.itemId],
                        it[Books.title],
                        it[Books.cover]
                    )
                }
                .first() // 첫 번째 행 반환

        // booksInfo 반환
        return booksInfo
    }

}