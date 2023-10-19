package com.example.commerce.order

import com.example.commerce.auth.Profiles
import com.example.commerce.books.Books
import com.example.commerce.books.Books.autoIncrement
import com.example.commerce.books.Books.uniqueIndex
import com.example.commerce.cart.Cart
import com.example.commerce.cart.Cart.autoIncrement
import com.example.commerce.cart.Cart.uniqueIndex
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

// 주문 테이블
object Order : Table("order") {
    val id = long("id").autoIncrement().uniqueIndex()

    // 주문일자
    val orderDate = datetime("order_date")

    // 주문상태 (1: 완료, 2:취소)
    val orderStatus = datetime("order_status")

    // 주문 key
    override val primaryKey = PrimaryKey(Order.id)
}


// 주문 Item
object OrderItem : Table("order_item") {
    val id = long("id").autoIncrement().uniqueIndex()

    // item 번호
    val itemId = integer("item_id")

    // 수량
    val quantity = integer("quantity")

    // 주문금액
    val orderPrice = integer("order_price")

    // 주문번호
    var orderId = reference("order_id", Order.id)


    // 주문 item key
    override val primaryKey = PrimaryKey(OrderItem.id)

}

// 배송지
object OrderAddress : Table("order_address") {
    val id = long("id").autoIncrement().uniqueIndex()

    // 우편번호
    val postcode = integer("postcode")

    // 기본주소
    val address = integer("address")

    // 상세주소
    val detailAddress = integer("detail_address")

    // 주문번호
    var orderId = reference("order_id", Order.id)

    // 주문 item key
    override val primaryKey = PrimaryKey(OrderAddress.id)

}

//테이블 생성
@Configuration
class OrderTableSetUp(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Order, OrderItem, OrderAddress)
        }
    }
}

