package com.example.commerce.books


import com.example.commerce.auth.Identities.varchar
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Service

data class BookTable (
    val id: Column<EntityID<Long>>,
    val publisher: Column<String>,
    val title: Column<String>,
    val link: Column<String>,
    val author: Column<String>,
    val pubDate: Column<String>,
    val description: Column<String>,
    val isbn: Column<String>,
    val isbn13: Column<String>,
    val itemId: Column<Int>,
    val priceSales: Column<Int>,
    val priceStandard: Column<Int>,
    val stockStatus:Column<String>,
    val cover: Column<String>,
    val categoryId: Column<Int>,
    val categoryName: Column<String>,
    val customerReviewRank : Column<Int>,
)

@Service
class BookService {
    //카운트별칭
    fun getComment(): ExpressionAlias<Long> {
        val c = BookComments
        return c.id.count().alias("commentCount")
    }

    //매핑 함수
    fun mapToBookResponse(
        table: BookTable, r: ResultRow, commentCountAlias: ExpressionAlias<Long>
    ): BookResponse {
        return BookResponse(
            r[table.id].value,
            r[table.publisher],
            r[table.title],
            r[table.link],
            r[table.author],
            r[table.pubDate],
            r[table.description],
            r[table.isbn],
            r[table.isbn13],
            r[table.itemId],
            r[table.priceSales],
            r[table.priceStandard],
            r[table.stockStatus],
            r[table.cover],
            r[table.categoryId],
            r[table.categoryName],
            r[commentCountAlias]
        )
    }


}