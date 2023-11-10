package com.example.commerce.order

import com.example.commerce.auth.Profiles
import com.example.commerce.books.Books
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

//ALTER TABLE Orders AUTO_INCREMENT = 2023123456789;
// 주문 테이블
object Orders : Table("orders") {
    val id = long("id").autoIncrement().uniqueIndex()

    // 주문일자
    val orderDate = datetime("order_date")

    // 결제수단
    val paymentMethod = varchar("payment_method", 1)

    // 결제금액
    val paymentPrice = integer("payment_price")

    // 주문상태 (1: 완료, 2:취소)
    val orderStatus = varchar("order_status", 1)

    val cancelMemo = varchar("cancel_memo", 50).nullable();

    val profileId = reference("profile_id", Profiles)

    // 주문 key
    override val primaryKey = PrimaryKey(Orders.id)
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
    var orderId = reference("order_id", Orders.id)

    // 도서정보와 Foreign key 설정
//    val itemId = reference("item_id", Books.itemId)

    // 주문 item key
    override val primaryKey = PrimaryKey(OrderItem.id)

}

// 배송지
object OrderAddress : Table("order_address") {
    val id = long("id").autoIncrement().uniqueIndex()

    // 배송자명
    val deliveryName = varchar("delivery_name", 50)

    // 배송자 핸드폰번호
    val deliveryPhone = varchar("delivery_phone", 20)

    // 우편번호
    val postcode = varchar("postcode", 6)

    // 기본주소
    val address = varchar("address", 100)

    // 상세주소
    val detailAddress = varchar("detail_address", 100)

    // 배송요청사항
    var deliveryMemo = varchar("delivery_memo", 500)

    // 주문번호
    var orderId = reference("order_id", Orders.id)

    // 주문 item key
    override val primaryKey = PrimaryKey(OrderAddress.id)

}

// 주문 판매 정보(도서별 판매량)
//object OrderSales : Table("order_sales") {
//    val id = long("id").autoIncrement().uniqueIndex()
//
//    // 도서별 판매수
//    val bookSales = integer("book_sales")
//
//    // 도서 Item Id
//    val itemId = integer("item_id")
//
//    val status = varchar("status", 1)
//
//    // 도서 Id
//    var bookId = reference("book_id", Books.id)
//
//    // key
//    override val primaryKey = PrimaryKey(OrderSales.id)
//}

//테이블 생성
@Configuration
class OrderTableSetUp(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Orders, OrderItem, OrderAddress)
        }
    }
}

