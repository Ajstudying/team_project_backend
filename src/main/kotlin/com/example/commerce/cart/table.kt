package com.example.commerce.cart

import com.example.commerce.auth.Profiles
import com.example.commerce.books.Books
import com.example.commerce.books.Books.autoIncrement
import com.example.commerce.books.Books.uniqueIndex
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

// 장바구니 테이블
//object Cart : Table("cart") {
//    val id = long("id").autoIncrement().uniqueIndex()
//    val profileId = reference("profile_id", Profiles)
//    val createDate = datetime("created_date")
//}

object Cart : Table("cart") {
    val id = long("id").autoIncrement().uniqueIndex()
    val profileId = reference("profile_id", Profiles)
    val createDate = datetime("created_date")
    override val primaryKey = PrimaryKey(Cart.id)
}


// 장바구니 도서목록
object CartItem : Table("cart_item") {
    val id = long("id").autoIncrement().uniqueIndex()

    // 장바구니 id
    val cartId = reference("cart_id", Cart.id)
    val itemId = integer("item_id")
    val quantity = integer("quantity")
    val createDate = datetime("created_date")

    // 장바구니 상태 (임시 필드)
    val cartStatus = varchar("cart_status", 1)
    override val primaryKey = PrimaryKey(CartItem.id)
}


//테이블 생성
@Configuration
class CartTableSetUp(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Cart, CartItem)
        }
    }
}

