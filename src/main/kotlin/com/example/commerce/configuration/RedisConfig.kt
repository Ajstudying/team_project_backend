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
//    ): RedisTemplate<Long, String> {
//        val template = RedisTemplate<Long, String>()
//        template.connectionFactory = redisConnectionFactory
//        return template
//    }
//}