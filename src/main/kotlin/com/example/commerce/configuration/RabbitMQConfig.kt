package com.example.commerce.configuration

import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary


@Configuration
class RabbitMQConfig {

    @Value("\${spring.rabbitmq.host}")
    private val defaultHost: String = ""

    @Value("\${second.rabbitmq.host}")
    private val secondHost: String = ""

    @Bean
    fun queue1() = Queue("hits-queue")

    @Bean
    fun queue2() = Queue("create-order")

    @Bean
    fun rabbitAdmin1(connectionFactory1: ConnectionFactory): RabbitAdmin {
        return RabbitAdmin(connectionFactory1)
    }
    @Bean
    fun rabbitAdmin2(connectionFactory2: ConnectionFactory) : RabbitAdmin {
        return RabbitAdmin(connectionFactory2)
    }

    @Bean
    @Primary
    fun connectionFactory1(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.setHost("192.168.100.94")
        connectionFactory.port = 5672
        connectionFactory.username = "rabbit"
        connectionFactory.setPassword("password1234!")

        return connectionFactory
    }

    @Bean
    fun rabbitListenerContainerFactory1(connectionFactory1: ConnectionFactory): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory1)
        // 다른 설정 추가 가능
        return factory
    }

    @Bean
    fun connectionFactory2(): ConnectionFactory {
        val connectionFactory = CachingConnectionFactory()
        connectionFactory.setHost("192.168.100.204")
        connectionFactory.port = 5672
        connectionFactory.username = "rabbit"
        connectionFactory.setPassword("password1234!")
        return connectionFactory
    }

    @Bean
    fun rabbitListenerContainerFactory2(connectionFactory2: ConnectionFactory): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory2)
        // 다른 설정 추가 가능
        return factory
    }


    @Bean
    fun rabbitTemplate1(connectionFactory1: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory1)
        // 메시지 컨버터 및 기타 구성 추가
        return rabbitTemplate
    }

    @Bean
    fun rabbitTemplate2(connectionFactory2: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory2)
        // 메시지 컨버터 및 기타 구성 추가
        return rabbitTemplate
    }

}