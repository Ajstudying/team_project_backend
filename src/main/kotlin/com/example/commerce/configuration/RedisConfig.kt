package com.example.commerce.configuration

import com.example.commerce.books.BookDataResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate

//@Configuration
//class RedisConfig {
//
//    @Bean
//    fun redisTemplate(
//        redisConnectionFactory: RedisConnectionFactory
//    ): RedisTemplate<String, String> {
//        val template = RedisTemplate<String, String>()
//        template.connectionFactory = redisConnectionFactory
//        return template
//    }
//}