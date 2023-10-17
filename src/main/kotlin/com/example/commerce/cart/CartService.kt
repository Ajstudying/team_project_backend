package com.example.commerce.cart

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CartService(private val database: Database) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun createCart(req: CartItemRequest) : Long {

        val cartId = transaction {
            try{
                // 장바구니 등록
                val cartId = Cart.insertAndGetId {
                    it[this.profileId] = req.profileId
                    it[this.createDate] = LocalDateTime.now()
                }

                // 장바구니 item 등록
                val cartItemId = CartItem.insertAndGetId {
                    it[this.cartId] = cartId
                    it[this.itemId] = req.itemId
                    it[this.qty] = req.qty
                    it[this.createDate] = LocalDateTime.now()
                    it[this.cartStatus] = "0"
                }
                return@transaction cartId.value

            }catch (e: Exception){
                rollback()
                //에러메세지 확인
                logger.error(e.message)
                return@transaction 0
            }
        }
        return cartId
    }

}