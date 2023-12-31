package com.example.commerce.auth

import com.example.commerce.auth.util.JwtUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/book-commerce/auth")
class AuthController(private val service: AuthService) {

    @PostMapping(value = ["/signup"])
    fun signUp(@RequestBody req: SignupRequest): ResponseEntity<Long> {
        //객체가 들어온건지 확인
        println(req)

        val profileId = service.createIdentity(req)
        if (profileId > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(profileId)
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(profileId)
        }
    }

    //referer 값으로 나눠진 클라이언트들의 로그인이 잘 되도록 했다.
    @PostMapping(value = ["/signin"])
    fun signIn(
        @RequestParam userid: String,
        @RequestParam password: String,
        // Referer : http://this.cloudfront.net/auth/login
        // Referer : http://that.cloudfront.net/auth/login
        @RequestHeader (value = "referer", required = false) referer: String,
        res: HttpServletResponse,
    ): ResponseEntity<*> {
        //값이 들어온 것을 확인
        println("====== /api/book-commerce/auth ==========")
        println(userid+","+ password)
        println(referer)

        println(referer.split("/")[2].split(":")[0])
        println("${referer.split("/")[0]}://${referer.split("/")[2]}")

        val (result, message) = service.authenticate(userid, password)
        println(result)
        if (result) {
            // cookie와 헤더를 생성한 후 리다이렉트
            val cookie = Cookie("token", message)
            cookie.path = "/"
            cookie.maxAge = (JwtUtil.TOKEN_TIMEOUT / 1000L).toInt() // 만료시간
            cookie.domain = referer.split("/")[2].split(":")[0] // 쿠키를 사용할 수 있는 도메인
//            cookie.domain = "localhost"

            // 응답헤더에 쿠키 추가
            res.addCookie(cookie)

            // 웹 첫페이지로 리다이렉트
            return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(
                    ServletUriComponentsBuilder
                        .fromHttpUrl("${referer.split("/")[0]}//${referer.split("/")[2]}")
                        .build().toUri()
                )
                .build<Any>()
        }
        // 오류 메시지 반환
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(
                ServletUriComponentsBuilder
                    .fromHttpUrl("$referer?err=$message")
                    .build().toUri()
            )
            .build<Any>()
    }


    @Auth
    @GetMapping(value = ["/profile"])
    fun fetch(@RequestAttribute authProfile: AuthProfile): AuthProfileExtends? = transaction {

        println("<<< AuthController (/) >>>")
        println("-- 입력값 확인 : authProfile.id : " + authProfile.id)

        val p = Profiles

        //프로필 정보조회
        val profileRecord: AuthProfileExtends? = p.select { p.identityId eq authProfile.id }
            .singleOrNull()
            ?.let { r ->
                AuthProfileExtends(
                    r[p.id].value,
                    r[p.nickname],
                    r[p.phone],
                    r[p.email],
                )
            }

        println("-- 결과값 확인 : " + profileRecord)

        return@transaction profileRecord
    }

}