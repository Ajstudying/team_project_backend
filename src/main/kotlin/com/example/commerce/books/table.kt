package com.example.commerce.books

import com.example.commerce.auth.Profiles
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

//책 테이블
object Books : Table("books") {
    val id = long("id").autoIncrement()
    val publisher = varchar("publisher",32)
    val title = varchar("title", 255)
    val link = varchar("link", 512)
    val author = varchar("author", 512)
    val pubDate = varchar("pub_date", 10)
    val description = varchar("description", 512)
    val isbn = varchar("isbn", 13)
    val isbn13 = varchar("isbn13", 13)
    val itemId = integer("item_id")
    val priceSales = integer("price_sales")
    val priceStandard = integer("price_standard")
    val stockStatus = varchar("stock_status", 20)
    val cover = varchar("cover", 512)
    val categoryId = integer("category_id")
    val categoryName = varchar("category_name", 255)
    val customerReviewRank = integer("customer_review_rank")
}

//댓글 테이블
object BookComments : LongIdTable("book_comment") {
    val bookId = reference("book_id", Books.id)
    val comment = text("comment")
    val profileId = reference("profile_id", Profiles)
}

//테이블 생성
@Configuration
class BookTableSetUp(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Books, BookComments)
        }
    }
}