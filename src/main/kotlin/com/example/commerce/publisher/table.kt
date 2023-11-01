package com.example.commerce.publisher

import com.example.commerce.books.*
import com.example.commerce.order.OrderAddress
import com.example.commerce.order.OrderItem
import com.example.commerce.order.OrderSales
import com.example.commerce.order.Orders
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object Publisher : LongIdTable("publisher") {
    //    val id = long("id").autoIncrement().uniqueIndex()
    val itemId = integer("item_id")
    val title = varchar("title", 512)
    val author = varchar("author", 512)
    val cover = varchar("cover", 512)
    val publisher = varchar("publisher", 512)
//    override val primaryKey = PrimaryKey(id)
}

object EventBooks : LongIdTable("event_books") {
    //    val id = long("id").autoIncrement().uniqueIndex()
    val itemId = integer("item_id")
    val authorImage = varchar("author_image", 512)
    val authorDesc = varchar("author_desc", 1000)
    val descTitle = varchar("desc_title", 512)
    val description = varchar("description", 1000)
    val descCover = varchar("desc_cover", 512)
    val quotation = varchar("quotation", 1000)
//    override val primaryKey = PrimaryKey(id)
}

@Configuration
class PublisherTableSetUp(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Publisher, EventBooks)
        }
    }
}
