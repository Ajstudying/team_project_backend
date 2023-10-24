package com.example.commerce.books


import com.example.commerce.auth.Identities.varchar
import com.example.commerce.auth.Profiles
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    //신간 도서 댓글 리스트 찾기
    fun getNewBooksComments(id: Long): List<BookCommentResponse> {
        val c = BookComments
        val pf = Profiles;
        val comments = transaction {
            (c innerJoin pf)
                .select{(c.newBookId eq id)}
                .orderBy(c.id to SortOrder.DESC)
                .mapNotNull { r -> BookCommentResponse (
                    r[c.id].value, r[c.comment],r[pf.nickname], r[c.createdDate],
                ) }
        }
        return comments
    }
    //신간 도서 상세 찾기
    fun getNewBook(id: Long, comments:  List<BookCommentResponse>): BookResponse? {
        val n = NewBooks

        val newBook = transaction {
            n.select { (NewBooks.id eq id) }
                .groupBy(n.id)  // groupBy 메소드로 그룹화할 기준 컬럼들을 지정
                .mapNotNull { r ->
                    //집계 함수식의 별칭 설정
                    val commentCount = comments.size.toLong()
                    BookResponse(
                        r[n.id].value, r[n.publisher], r[n.title], r[n.link], r[n.author], r[n.pubDate],
                        r[n.description], r[n.isbn], r[n.isbn13], r[n.itemId], r[n.priceSales],
                        r[n.priceStandard], r[n.stockStatus], r[n.cover], r[n.categoryId], r[n.categoryName],
                        commentCount= commentCount, bookComment = comments,
                    ) }
                .singleOrNull() }

        return newBook
    }

    //신간 댓글 찾기
    fun findComment (id: Long, profileId: Long) : ResultRow? {
        val comment = transaction {
            BookComments
                .select (where = (BookComments.id eq id) and
                        (BookComments.profileId eq profileId)).firstOrNull() }
        return comment
    }

    //좋아요 추가/수정
    fun updateLikeRecord (id: Long, profileId: Long, likes: Boolean, newBookId: Int?) {
        val findLike = transaction {
            LikeBooks
                .select(where = (if (newBookId != null) LikeBooks.newBookId else LikeBooks.bookId) eq id and (LikeBooks.profileId eq profileId))
                .firstOrNull()
        }

        if (findLike != null) {
            transaction {
                LikeBooks.update {
                    it[LikeBooks.likes] = likes
                    it[LikeBooks.profileId] = profileId
                    if (newBookId != null) {
                        it[LikeBooks.newBookId] = id
                    } else {
                        it[bookId] = id
                    }
                }
            }
        } else {
            transaction {
                LikeBooks.insert {
                    it[LikeBooks.likes] = likes
                    it[LikeBooks.profileId] = profileId
                    if (newBookId != null) {
                        it[LikeBooks.newBookId] = id
                    } else {
                        it[bookId] = id
                    }
                }
            }
        }
    }

    //매핑 함수
//    fun mapToBookResponse(
//        table: BookTable, r: ResultRow, commentCountAlias: ExpressionAlias<Long>
//    ): BookResponse {
//        return BookResponse(
//            r[table.id].value,
//            r[table.publisher],
//            r[table.title],
//            r[table.link],
//            r[table.author],
//            r[table.pubDate],
//            r[table.description],
//            r[table.isbn],
//            r[table.isbn13],
//            r[table.itemId],
//            r[table.priceSales],
//            r[table.priceStandard],
//            r[table.stockStatus],
//            r[table.cover],
//            r[table.categoryId],
//            r[table.categoryName],
//            r[commentCountAlias]
//        )
//    }


}