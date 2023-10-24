package com.example.commerce.cart

import com.example.commerce.auth.AuthProfile
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CartService(private val database: Database) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    // 장바구니 생성, 장바구니 item 생성
    fun createCart(req: CartItemRequest, authProfile: AuthProfile): Long {

        println("\n<<< CartService createCart>>>")
        println(" -- 입력값 확인 :  " + authProfile.id)

        val cartId = transaction {
            try {

                // 해당 도서가 이미 장바구니에 담겨져 있는지 체크한다.
                // SQL
                // select count(*) from cart
                // join cart_item on cart.id = cart_item.cart_id
                // where cart.profile_id = 1 and cart_item = :cart_item;
                val countCartItem =
                        Cart
                                .join(CartItem, JoinType.INNER, onColumn = CartItem.cartId, otherColumn = Cart.id)
                                .select { (Cart.profileId eq authProfile.id) and (CartItem.itemId eq req.itemId) }
                                .count().toInt()

                println("해당 도서가 이미 장바구니에 담겨져 있는지 체크한다.(countCartItem) >> " + countCartItem);

                // 해당 도서가 이미 장바구니에 담겨져 있으면 리턴
                if (countCartItem > 0) {
                    println("해당 도서가 이미 장바구니에 담겨져 있음")
                    return@transaction 0
                }

                // 등록된 장바구니(Cart)가 있는지 확인, 없으면 Cart 생성
                // SQL
                // select id from cart where profile_id = :authprofile.id
                var cartRecord =
                        Cart.slice(Cart.id).select { Cart.profileId eq authProfile.id }
                                .firstOrNull()

                val existCartId = cartRecord?.get(Cart.id) ?: 0
                println(" -- 등록된 장바구니 id(existCartId) $existCartId")

                var cartId = 0L
                if (existCartId < 1) {
                    // 장바구니 등록
                    cartId = Cart.insert {
                        it[this.profileId] = authProfile.id;
                        it[this.createDate] = LocalDateTime.now()
                    } get Cart.id // Explicitly specify the Cart.id column
                } else {
                    cartId = existCartId
                }
                println(" -- 장바구니 등록 cartId : " + cartId.toString())

                var cartItemId = CartItem.insert {
                    it[this.cartId] = cartId
                    it[this.itemId] = req.itemId
                    it[this.quantity] = req.quantity
                    it[this.createDate] = LocalDateTime.now()
                } get CartItem.id

                println(" -- 장바구니 등록 cartItemId : " + cartItemId)

                return@transaction cartId

            } catch (e: Exception) {
                rollback()
                //에러메세지 확인
                logger.error(e.message)
                return@transaction 0
            }
        }
        return cartId
    }

    // 장바구니 삭제, 장바구니 item 삭제
    fun deleteCart(itemId: Int, authProfile: AuthProfile): Long {

        // SQL
        // select id from cart where profile_id = :authprofile.id
        var cartId = 0L;
        var cartRecord = transaction {
            Cart.slice(Cart.id).select { Cart.profileId eq authProfile.id }
                    .first()
                    .let { r -> cartId = r[Cart.id] }
        }
        if (cartRecord == null) {
            return 0
        }

        println("삭제 대상 cart_id : " + cartId)

        // SQL
        // select count(*) from cart_item where cart_id = :cartId
        var countCartItem = transaction {
            CartItem.select { CartItem.cartId eq cartId }
                    .count()
        }

        transaction {
            try {
                // cart_item이 1개만 존재하면 -> cart_item 삭제, cart 삭제
                if (countCartItem < 2) {

                    // delete from cart_item where cart_id = :cartId and item_id = :itemId
                    CartItem.deleteWhere { (CartItem.cartId eq cartId) and (CartItem.itemId eq itemId) }

                    // delete from cart where profile_id = :authprofile.id
                    Cart.deleteWhere { Cart.profileId eq authProfile.id }
                } else {
                    // cart_item이 1개 이상이면 -> cart_item 만 삭제

                    // delete from cart_item where cart_id = :cartId and item_id = :itemId
                    CartItem.deleteWhere { (CartItem.cartId eq cartId) and (CartItem.itemId eq itemId) }
                }
            } catch (e: Exception) {
                rollback()
                //에러메세지 확인
                logger.error(e.message)
                return@transaction 0
            }
        }
        return 1
    }
}