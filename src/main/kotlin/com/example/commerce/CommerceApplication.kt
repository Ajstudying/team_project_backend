package com.example.commerce

import com.example.commerce.books.BookResponse
import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
class CommerceApplication

fun main(args: Array<String>) {
	runApplication<CommerceApplication>(*args)
}

//의존성 레디스 템플릿이 생성되면 해당 로그를 찍는 방법↓
//@Configuration
//class RedisConfig(private val redisTemplate: RedisTemplate<String, String>){
//	@PostConstruct
//	fun getConnection(){
//		println(redisTemplate.connectionFactory?.connection)
//	}
//}