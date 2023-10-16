package com.example.commerce.auth

import com.example.commerce.auth.util.Auth
import com.example.commerce.auth.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.reflect.Method

@Component
class AuthInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest,
                           resonse: HttpServletResponse,
                           handler: Any): Boolean {

        if(handler is HandlerMethod) {
            val handlerMethod: HandlerMethod = handler
            val method: Method = handlerMethod.method

            //Auth어노테이션 확인
            if(method.getAnnotation(Auth::class.java) == null){
                return true
            }
            //토큰 읽기
            val token: String? = request.getHeader("Authorization")
            //제대로 인터셉터 됐는지, 토큰이 제대로 서버에 들어왔는지 확인하기 위한 print
            println(token)
            //토큰이 없으면 미인증
            if(token.isNullOrEmpty()){
                resonse.status = 401
                return false
            }

            val profile: AuthProfile? =
                JwtUtil.validateToken(token.replace("Bearer", ""))
            if(profile == null) {
                //인증 토큰 오류
                resonse.status = 401
                return false
            }
            //이게 없으면 미인증, 있으면 토큰 오류
            println(profile)

            request.setAttribute("authProfile", profile)
            return true
        }

        return true
    }
}