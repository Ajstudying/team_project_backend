package com.example.commerce.cart

import com.example.commerce.auth.Auth
import com.example.commerce.auth.AuthProfile
import com.example.commerce.books.Books
import com.example.commerce.cart.CartItem.cartId
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

//@Tag(name="장바구니 처리 API")
@RestController
@RequestMapping("/cart")
class CartController(private val service: CartService) {

    // 장바구니 생성
    @Auth
    @PostMapping(value = ["/add"])
    fun create(@RequestBody request: CartItemRequest, @RequestAttribute authProfile: AuthProfile):
            ResponseEntity<Long> {

        println("<<< CartController /cart/add >>>")
        println("입력 값 확인")
        println(
            "profileId:" + authProfile.userid.toString() +
                    ",itemId:" + request.itemId.toString() + ",qty:" +
                    request.qty
        )

        // 장바구니 등록 후 cartId return
        val cartId = service.createCart(request)

        return if (cartId > 0) {
            ResponseEntity.status(HttpStatus.CREATED).body(cartId)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(cartId)
        }
    }

    // 장바구니 목록 조회
    @Auth
    @GetMapping
    fun fetch(@RequestAttribute authProfile: AuthProfile) = transaction() {

        println("<<< CartController (/cart) >>>")
        println("-- 입력값 확인 : authProfile.id : " + authProfile.id)

        // SELECT
        //  t1.id, t1.cart_id,
        //  t2.item_id, t2.title, t2.cover, t2.author, t2.pricestandard, t2.pricesales,
        //  t1.qty
        // FROM cart_item t1
        // JOIN books t2 ON t1.item_id = t2.item_id;
        var p = CartItem
        var b = Books

        CartItem
            .select(where = (p.itemId eq b.itemId))
            .orderBy(p.createDate to SortOrder.DESC)
            .map { it ->
                CartItemResponse(
                    it[p.itemId],
                    it[b.title],
                    it[b.cover],
                    it[b.author],
                    it[b.priceStandard],
                    it[b.priceSales],
                    it[p.qty]
                )
            }
    }


}