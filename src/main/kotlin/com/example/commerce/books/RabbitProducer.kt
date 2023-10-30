package com.example.commerce.books

import com.example.commerce.inventory.HitsTable
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

//@Service
//class RabbitProducer(private val rabbitTemplate: RabbitTemplate) {
//    private val mapper = jacksonObjectMapper()
//    fun sendHits(hits: HitsTable){
//        rabbitTemplate.convertAndSend("hits-queue", mapper.writeValueAsString(hits))
//    }
//}