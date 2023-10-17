package com.example.commerce.cart

import com.example.commerce.auth.Profiles
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

// 장바구니 테이블
object Cart : LongIdTable("cart") {

}
// 주문 테이블
object Order : LongIdTable("order") {

}

//테이블 생성
@Configuration
class CartTableSetUp(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Cart, Order)
        }
    }
}