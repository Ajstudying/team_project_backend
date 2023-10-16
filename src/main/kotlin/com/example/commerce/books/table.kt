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
object Books : LongIdTable("books") {
    val publisher = varchar("publisher",32)
    val title = varchar("title", 255)
    val link = varchar("link", 512)
    val author = varchar("author", 512)
    val pubDate = varchar("pubDate", 10)
    val description = varchar("description", 512)
    val isbn = varchar("isbn", 13)
    val isbn13 = varchar("isbn13", 13)
    val itemId = integer("itemId")
    val priceSales = integer("priceSales")
    val priceStandard = integer("priceStandard")
    val mallType = varchar("mallType", 50)
    val stockStatus = varchar("stockStatus", 20)
    val cover = varchar("cover", 512)
    val categoryId = integer("categoryId")
    val categoryName = varchar("categoryName", 255)
    val customerReviewRank = integer("customerReviewRank")
}

//댓글 테이블
object BookComments : LongIdTable("book_comment") {
    val bookId = reference("itemId", Books.itemId)
    val comment = text("comment")
    val profileId = reference("profile_id", Profiles)
    val nickname = reference("nickname", Profiles.nickname)
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