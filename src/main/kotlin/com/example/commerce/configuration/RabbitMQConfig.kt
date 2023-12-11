package com.example.commerce.configuration

import jakarta.annotation.PostConstruct
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class RabbitMQConfig {

    @Value("\${spring.rabbitmq.addresses}")
    private val defaultHost: String = ""

    @Value("\${second.rabbitmq.addresses}")
    private val secondHost: String = ""

    @PostConstruct
    fun printBeanCreationOrder() {
        println("Bean creation order: defaultHost=$defaultHost, secondHost=$secondHost")
    }

    @Bean
    fun queue1() = Queue("create-order")

    @Bean
    fun queue2() = Queue("hits-queue")

    @Bean("connectionFactory1")
    @Primary
    fun connectionFactory1(): ConnectionFactory {
        println("첫 실행")
        println(defaultHost)
        val connectionFactory = CachingConnectionFactory()
//        connectionFactory.setHost("192.168.100.204")
//        connectionFactory.setHost("192.168.0.5")
//        connectionFactory.setHost("192.168.100.177")
//        connectionFactory.setHost("192.168.100.36")
//        connectionFactory.setHost("192.168.100.155")
//        connectionFactory.port = 5672
        connectionFactory.setAddresses(defaultHost)
        connectionFactory.username = "rabbit"
        connectionFactory.setPassword("password1234!")

        return connectionFactory
    }

    //레빗 메세지 받는 컨테이너 팩토리
//    @Bean
//    fun rabbitListenerContainerFactory1(@Qualifier("connectionFactory1") connectionFactory1: ConnectionFactory): SimpleRabbitListenerContainerFactory {
//        val factory = SimpleRabbitListenerContainerFactory()
//        factory.setConnectionFactory(connectionFactory1)
//        // 다른 설정 추가 가능
//        return factory
//    }

    @Bean("connectionFactory2")
    fun connectionFactory2(): ConnectionFactory {
        println("두번째 실행")
        println(secondHost)
        val connectionFactory = CachingConnectionFactory()
//        connectionFactory.setHost("192.168.100.94")
//        connectionFactory.port = 5672
        connectionFactory.setAddresses(secondHost)
        connectionFactory.username = "rabbit"
        connectionFactory.setPassword("password1234!")
        return connectionFactory
    }

    //메세지 받는 컨테이너 팩토리
//    @Bean
//    fun rabbitListenerContainerFactory2(@Qualifier("connectionFactory2") connectionFactory2: ConnectionFactory): SimpleRabbitListenerContainerFactory {
//        val factory = SimpleRabbitListenerContainerFactory()
//        factory.setConnectionFactory(connectionFactory2)
//        // 다른 설정 추가 가능
//        return factory
//    }

//    @Bean
//    fun rabbitAdmin1(connectionFactory1: ConnectionFactory): RabbitAdmin {
//        return RabbitAdmin(connectionFactory1)
//    }
//
//    @Bean
//    fun rabbitAdmin2(connectionFactory2: ConnectionFactory): RabbitAdmin {
//        return RabbitAdmin(connectionFactory2)
//    }


    @Bean("rabbitTemplate1")
    fun rabbitTemplate1(@Qualifier("connectionFactory1") connectionFactory1: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory1)
        // 메시지 컨버터 및 기타 구성 추가
        return rabbitTemplate
    }

    @Bean("rabbitTemplate2")
    fun rabbitTemplate2(@Qualifier("connectionFactory2") connectionFactory2: ConnectionFactory): RabbitTemplate {
        val rabbitTemplate = RabbitTemplate(connectionFactory2)
        // 메시지 컨버터 및 기타 구성 추가
        return rabbitTemplate
    }

}