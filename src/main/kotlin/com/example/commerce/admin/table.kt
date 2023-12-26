package com.example.commerce.admin

import com.example.commerce.auth.Profiles
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

//오늘의 책 저장 테이블
object TodayBook: LongIdTable("today_book"){
    val cover = varchar("cover", 512)
    val title = varchar("title", 512)
    val author = varchar("author", 512)
    val priceSales = integer("price_sales")
    val todayLetter = varchar("today_letter", 512)
    val itemId = integer("item_id")
    val createdDate = varchar("created_date",32)
}

//조회수 저장 테이블
object HitsTable: LongIdTable("hits_table"){
    val itemId = integer("item_id")
    val profileId = reference("profile_id", Profiles).nullable()
    val hitsCount = long("hits_count")
    val createdDate = varchar("created_date",32)
}

object MainFiles : LongIdTable("main_files"){
    val image = varchar("image", 512)
    val link = varchar("link", 512)
}

//테이블 생성
@Configuration
class InventoryTableSetUp(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(
                HitsTable, TodayBook
            )
        }
    }
}