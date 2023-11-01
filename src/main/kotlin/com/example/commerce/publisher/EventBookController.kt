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
        val p = Publisher
        val e = EventBooks

        p.selectAll().map { r ->
            EventBookResponse(
                r[p.id].value,
                r[p.itemId],
                r[p.title],
                r[p.author],
                r[e.authorImage],
                r[e.authorDesc],
                r[p.cover],
                r[e.descTitle],
                r[e.description],
                r[e.descCover],
                r[p.publisher],
            )
        }
    }
}
