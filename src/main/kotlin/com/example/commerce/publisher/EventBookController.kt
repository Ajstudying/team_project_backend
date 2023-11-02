package com.example.commerce.publisher

import com.example.commerce.books.Books
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/publisher")
class EventBookController {

    @GetMapping("/event")
    fun fetch() = transaction() {

        val e = EventBooks

        (EventBooks)
                .join(Books, JoinType.INNER, onColumn = Books.itemId, otherColumn = EventBooks.itemId)
                .slice(Books.id, e.itemId, e.title, e.description, e.cover, e.textSentence,
                        e.mentSentence, e.authorImage, e.author, e.publisher, e.authorDescription)
                .select { e.title neq "" }
                .map { r ->
                    EventBookResponse(
                            r[Books.id].value,
                            r[e.itemId],
                            r[e.title],
                            r[e.description],
                            r[e.cover],
                            r[e.textSentence],
                            r[e.mentSentence],
                            r[e.authorImage],
                            r[e.author],
                            r[e.publisher],
                            r[e.authorDescription],
                    )
                }

//        e.selectAll().map { r ->
//            EventBookResponse(
//                    r[e.id].value,
//                    r[e.itemId],
//                    r[e.title],
//                    r[e.description],
//                    r[e.cover],
//                    r[e.textSentence],
//                    r[e.mentSentence],
//                    r[e.authorImage],
//                    r[e.author],
//                    r[e.publisher],
//                    r[e.authorDescription],
//            )
//        }
    }
}
