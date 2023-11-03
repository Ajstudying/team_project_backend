package com.example.commerce.cart

import com.example.commerce.auth.Auth
import com.example.commerce.auth.AuthProfile
import com.example.commerce.auth.Identities
import com.example.commerce.books.Books
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.sql.Connection

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
                        ",itemId:" + request.itemId.toString() + ",quantity:" +
                        request.quantity
        )

        // 필요한 request 값이 빈값이면 400 : Bad request
        if (!request.validate()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400)
        }

        // 장바구니 등록 후 cartId return
        val cartId = service.createCart(request, authProfile)

        return if (cartId > 0) {
            ResponseEntity.status(HttpStatus.CREATED).body(cartId)
        } else {
            ResponseEntity.status(HttpStatus.CONFLICT).body(cartId)
        }
    }

    @Auth
    @DeleteMapping("/delete/{itemId}")
    fun remove(@PathVariable itemId: Int,
               @RequestAttribute authProfile: AuthProfile): ResponseEntity<Any> {
        println("<<< CartController /cart/delete >>>")
        println("입력 값 확인")
        println(
                "profileId:" + authProfile.userid.toString() +
                        ",itemId:" + itemId.toString()
        )

        // 필요한 request 값이 빈값이면 400 : Bad request
        if (itemId < 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(400)
        }

        // 장바구니 등록 후 cartId return
        val cartId = service.deleteCart(itemId, authProfile)

        println("delete 결과 : " + cartId)

        return if (cartId > 0) {
            // 200 OK
            return ResponseEntity.ok().build()
        } else {
            // 해당 도서가 북마스터에 없으면, 404 에러 리턴
            if (cartId.toInt() === -404) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
            } else {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build()
            }

        }
    }

    @Auth
    @GetMapping("/count/{itemId}")
    fun isExistCartItem(@PathVariable itemId: Int, @RequestAttribute authProfile: AuthProfile): Boolean =
            transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {

                println("<<< CartController /cart/count >>>")
                println("입력 값 확인")
                println(
                        "profileId:" + authProfile.id.toString() +
                                ",itemId:" + itemId
                )

//                var iitemId: Int = 0
//                iitemId = Integer.parseInt(itemId);

                var isExistCartItem: Boolean = false;

                // 해당 도서가 이미 장바구니에 담겨져 있는지 체크한다.
                //        select count(*) from cart
                //        join cart_item on cart.id = cart_item.cart_id
                //                where cart.profile_id = 1 and cart_item = "";
                val countCart =
                        Cart
                                .join(CartItem, JoinType.INNER, onColumn = CartItem.cartId, otherColumn = Cart.id)
                                .select { (Cart.profileId eq authProfile.id) and (CartItem.itemId eq itemId) }
                                .count().toInt()

                println("countCart >> " + countCart);

                // 해당 도서가 이미 장바구니에 담겨져 있으면 리턴
                if (countCart > 0) {
                    println("해당 도서가 이미 장바구니에 담겨져 있음")
                    isExistCartItem = true;

                }
                return@transaction isExistCartItem
            }

    // 장바구니 목록 조회
    @Auth
    @GetMapping
    fun fetch(@RequestAttribute authProfile: AuthProfile): List<CartItemResponse> =
            transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {

                println("<<< CartController (/cart) >>>")
                println("-- 입력값 확인 : authProfile.id : " + authProfile.id)

                // object의 이름 alias 주기
                var c = Cart
                var ci = CartItem
                var b = Books
                var i = Identities

                // Join 조건
                // 장바구니 item_id = 도서 item_id
                // 장바구니 id = 회원 id
                // 회원 id = 로그인 userid
                val result = transaction {
                    (Cart innerJoin CartItem)
                            .join(Books, JoinType.INNER, onColumn = ci.itemId, otherColumn = b.itemId)
                            .join(Identities, JoinType.INNER, onColumn = c.profileId, otherColumn = i.id)
                            .slice(
                                    b.id,
                                    ci.itemId,
                                    b.title,
                                    b.cover,
                                    b.author,
                                    b.priceStandard,
                                    b.priceSales,
                                    b.categoryName,
                                    ci.quantity
                            )
                            .select { ci.cartId eq c.id }
                            .andWhere { i.userid eq authProfile.userid }
                            .map { r ->
                                CartItemResponse(
                                        r[b.id].value,
                                        r[ci.itemId],
                                        r[b.title],
                                        r[b.cover],
                                        r[b.author],
                                        r[b.priceStandard],
                                        r[b.priceSales],
                                        r[b.categoryName],
                                        r[ci.quantity]
                                )
                            }
                }

                println("-- 결과값 확인 : " + result)

                return@transaction result
            }

    @Auth
    @GetMapping("/books/count/{itemId}")
    fun isExistBookItem(@PathVariable itemId: Int, @RequestAttribute authProfile: AuthProfile): Boolean =
            transaction(Connection.TRANSACTION_READ_UNCOMMITTED, readOnly = true) {

                println("<<< CartController /books/cart/count >>>")
                println("입력 값 확인")
                println(
                        "profileId:" + authProfile.id.toString() +
                                ",itemId:" + itemId
                )

                var isExistBookItem: Boolean = false;

                // 해당 도서가 북마스터에 존재하는지 체크한다.
                // SQL
                // select count(*) from books
                // where item_id = :item_id;
                val countBooksItem =
                        Books
                                .select { (Books.itemId eq itemId) }
                                .count().toInt()

                println("해당 도서가 북마스터에 존재하는지 체크한다.(countBooksItem) >> " + countBooksItem);

                // 해당 도서가 북마스터에 없으면...
                if (countBooksItem === 0) {
                    println("*** 해당 도서가 북마스터 정보에 없습니다. Books 테이블에 확인 필요 >>> itemId:" + itemId)
                    return@transaction false
                }

                println("countBooksItem >> " + countBooksItem);

                return@transaction true
            }

}