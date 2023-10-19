package com.example.commerce.order

import com.example.commerce.auth.Auth
import com.example.commerce.auth.AuthProfile
import com.example.commerce.auth.Identities
import com.example.commerce.books.Books
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.jetbrains.exposed.sql.*
import java.sql.Connection

//@Tag(name="주문 처리 API")
@RestController
@RequestMapping("/order")
class OrderController(private val service: OrderService) {

    // 주문 생성
    @Auth
    @PostMapping(value = ["/add"])
    fun create(@RequestBody request: CartItemRequest, @RequestAttribute authProfile: AuthProfile):
            ResponseEntity<Long> {

        println("<<< OrderController /order/add >>>")
        println("입력 값 확인")
        println(
            "profileId:" + authProfile.userid.toString() +
                    ",itemId:" + request.itemId.toString() + ",quantity:" +
                    request.quantity
        )

        // 필요한 request 값이 빈값이면 400 : Bad request
        if (!request.validate()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400)
        }

        // 장바구니 등록 후 cartId return
        val cartId = service.createOrder(request, authProfile)

        return if (cartId > 0) {
            ResponseEntity.status(HttpStatus.CREATED).body(cartId)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(cartId)
        }
    }

    // 주문 목록 조회
    @Auth
    @GetMapping
    fun fetch(@RequestAttribute authProfile: AuthProfile): List<CartItemResponse> =
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {

            println("<<< OrderController (/order) >>>")
            println("-- 입력값 확인 : authProfile.id : " + authProfile.id)

            val result = mutableListOf<CartItemResponse>()

//            // object의 이름 alias 주기
//            var c = Cart
//            var ci = CartItem
//            var b = Books
//            var i = Identities
//
//            // Join 조건
//            // 장바구니 item_id = 도서 item_id
//            // 장바구니 id = 회원 id
//            // 회원 id = 로그인 userid
//            val result = transaction {
//                (Cart innerJoin CartItem)
//                    .join(Books, JoinType.INNER, onColumn = ci.itemId, otherColumn = b.itemId)
//                    .join(Identities, JoinType.INNER, onColumn = c.profileId, otherColumn = i.id)
//                    .slice(
//                        ci.itemId,
//                        b.title,
//                        b.cover,
//                        b.author,
//                        b.priceStandard,
//                        b.priceSales,
//                        b.categoryName,
//                        ci.quantity
//                    )
//                    .select { ci.cartId eq c.id }
//                    .andWhere { i.userid eq authProfile.userid }
//                    .map { r ->
//                        CartItemResponse(
//                            r[ci.itemId],
//                            r[b.title],
//                            r[b.cover],
//                            r[b.author],
//                            r[b.priceStandard],
//                            r[b.priceSales],
//                            r[b.categoryName],
//                            r[ci.quantity]
//                        )
//                    }
//            }


            println("-- 결과값 확인 : " + result)

            return@transaction result
        }

}