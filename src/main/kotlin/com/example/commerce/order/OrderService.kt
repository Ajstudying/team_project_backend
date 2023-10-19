package com.example.commerce.order

import com.example.commerce.auth.AuthProfile
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService(private val database: Database) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun createOrder(req: CartItemRequest, authProfile: AuthProfile): Long {

        println("\n<<< CartService createCart>>>")
        println(" -- 입력값 확인 :  " + authProfile.id)

        val orderId = 1L;

//        var insertCartId = 0L;
//        val existCartId = transaction {
//            Cart.slice(Cart.id)
//                .select { Cart.profileId eq authProfile.id }
//                .singleOrNull()
//        }
//
//        val cartId = transaction {
//            try {
//
//                // 장바구니 id가 생성되어 있지 않으면 등록함
//                if (existCartId == null) {
//                    // 장바구니 등록
//                    insertCartId = Cart.insert {
//                        it[this.profileId] = authProfile.id;
//                        it[this.createDate] = LocalDateTime.now()
//                    } get Cart.id // Explicitly specify the Cart.id column
//
//                    println(" -- 장바구니 등록 insertCartId : " + insertCartId.toString())
//                } else {
//                    insertCartId = existCartId[Cart.id];
//
//                    println(" -- 장바구니 이미 존재 : " + insertCartId.toString())
//                }
//
//                // 장바구니 item 등록
//                val cartItemId = CartItem.insert {
//                    it[this.cartId] = insertCartId
//                    it[this.itemId] = req.itemId
//                    it[this.quantity] = req.quantity
//                    it[this.createDate] = LocalDateTime.now()
//                    it[this.cartStatus] = "0"
//                } get CartItem.id
//
//                println(" -- 장바구니 등록 cartItemId : " + cartItemId)
//
//
//                return@transaction insertCartId
//
//            } catch (e: Exception) {
//                rollback()
//                //에러메세지 확인
//                logger.error(e.message)
//                return@transaction 0
//            }
//        }
        return orderId;
    }

}