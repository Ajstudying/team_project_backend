package com.example.commerce.auth.util

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.example.commerce.auth.AuthProfile
import java.util.Date

object JwtUtil {
    var secret = "your-secret"

    val TOKEN_TIMEOUT = (1000 * 60 * 60 * 24 * 7).toLong()

    //토큰 생성
    fun createToken(id: Long, userid: String, nickname:String): String {
        val now = Date()

        val exp = Date(now.time + TOKEN_TIMEOUT)
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.create()
            .withSubject(id.toString())
            .withClaim("userid", userid)
            .withClaim("nickname", nickname)
            .withIssuedAt(now)
            .withExpiresAt(exp)
            .sign(algorithm)
    }

    fun validateToken(token: String): AuthProfile? {

        val algorithm = Algorithm.HMAC256(secret)
        //검증 객체 생성
        val verifier: JWTVerifier = JWT.require(algorithm).build()
        return try {
            val decodedJWT : DecodedJWT = verifier.verify(token)
            val id: Long = java.lang.Long.valueOf(decodedJWT.subject)
            val nickname: String = decodedJWT.getClaim("nickname").asString()
            val userid: String = decodedJWT.getClaim("userid").asString()

            AuthProfile(id, nickname, userid)

        }catch (e: JWTVerificationException){
            //토큰 검증 오류 상황
            null
        }
    }

}