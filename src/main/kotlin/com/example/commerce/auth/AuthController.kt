package com.example.commerce.auth

import com.example.commerce.auth.util.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/auth")
class AuthController(private val service: AuthService) {

    @PostMapping(value = ["/signup"])
    fun signUp(@RequestBody req: SignupRequest): ResponseEntity<Long>{
        //객체가 들어온건지 확인
        println(req)

        val profileId = service.createIdentity(req)
        if(profileId > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(profileId)
        }else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(profileId)
        }
    }

    @PostMapping(value = ["/signin"])
    fun signIn(
        @RequestParam username: String,
        @RequestParam password: String,
        res: HttpServletResponse,
    ): ResponseEntity<*> {
        //값이 들어온 것을 확인
        println(username)
        println(password)

        val (result, message) = service.authenticate(username, password)
        println(result)
        if(result) {
            // cookie와 헤더를 생성한 후 리다이렉트
            val cookie = Cookie("token", message)
            cookie.path = "/"
            cookie.maxAge = (JwtUtil.TOKEN_TIMEOUT / 1000L).toInt() // 만료시간
            cookie.domain = "localhost"

            // 응답헤더에 쿠키 추가
            res.addCookie(cookie)

            // 웹 첫페이지로 리다이렉트
            return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                    ServletUriComponentsBuilder
                        .fromHttpUrl("http://localhost:5500")
                        .build().toUri()
                )
                .build<Any>()
        }
        // 오류 메시지 반환
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(
                ServletUriComponentsBuilder
                    .fromHttpUrl("http://localhost:5500/login.html?err=$message")
                    .build().toUri()
            )
            .build<Any>()
    }
}