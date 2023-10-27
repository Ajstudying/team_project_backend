package com.example.commerce.inventory

import com.example.commerce.books.*
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object TodayBook: LongIdTable("today_book"){
    val cover = varchar("cover", 512)
    val title = varchar("title", 512)
    val author = varchar("author", 512)
    val priceSales = integer("price_sales")
    val todayLetter = varchar("today_letter", 512)
    val bookId = reference("book_id", Books.id)
    val itemId = integer("item_id")
}

//테이블 생성
//@Configuration
//class BookTableSetUp(private val database: Database) {
//    @PostConstruct
//    fun migrateSchema() {
//        transaction(database) {
//            SchemaUtils.createMissingTablesAndColumns(
//                TodayBook
//            )
//        }
//    }
//}