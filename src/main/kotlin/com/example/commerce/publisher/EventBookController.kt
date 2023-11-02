package com.example.commerce.publisher

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

        e.selectAll().map { r ->
            EventBookResponse(
                    r[e.id].value,
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
    }
}
