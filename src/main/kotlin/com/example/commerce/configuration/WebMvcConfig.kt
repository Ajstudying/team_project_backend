package com.example.commerce.configuration

import com.example.commerce.auth.AuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebMvcConfig(val authInterceptor: AuthInterceptor) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
                .addMapping("/**")
                .allowedOrigins(
                    "http://localhost:5500",
                    "http://127.0.0.1:5500",
                    "http://localhost:5000",
                    "https://d7gp93w7wekd9.cloudfront.net",
                    "https://d1naeto6zidndq.cloudfront.net",
                    "http://ec2-15-164-111-91.ap-northeast-2.compute.amazonaws.com",
                ).allowedMethods("*") // 모든 메서드 허용(GET, POST.....)
    }

    // 인증처리용 인터셉터를 추가
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
    }
}