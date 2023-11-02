package com.example.commerce.admin

import com.example.commerce.auth.Profiles
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
    val itemId = integer("item_id")
    val createdDate = varchar("created_date",32)
}

object HitsTable: LongIdTable("hits_table"){
    val itemId = integer("item_id")
    val profileId = reference("profile_id", Profiles).nullable()
    val hitsCount = long("hits_count")
    val createdDate = varchar("created_date",32)
}

object MainFiles : LongIdTable("main_files"){
    val originalFileName = varchar("original_file_name", 200)
    val uuidFileName = varchar("uuid", 50).uniqueIndex()
    val contentType = varchar("content_type", 100)
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