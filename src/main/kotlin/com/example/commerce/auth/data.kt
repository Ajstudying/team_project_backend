package com.example.commerce.auth

data class SignupRequest(
    val userid: String,
    val password: String,
    val nickname: String,
    val phone: String,
    val email: String,
    val birth: String,
    val bookmark: String,
)

data class AuthProfile(
    val id: Long = 0, // 프로필 id
    val nickname: String, // 프로필 별칭
    val userid: String, // 로그인 사용자이름
)

data class AuthProfileExtends(
    val profileId: Long,
    val nickname: String,
    val phone: String,
    val email: String,
)