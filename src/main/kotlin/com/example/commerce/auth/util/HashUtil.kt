package com.example.commerce.auth.util

import at.favre.lib.crypto.bcrypt.BCrypt

object HashUtil {
    //해시 생성
    fun createHash(cipherText: String): String {
        return BCrypt.withDefaults().hashToString(12, cipherText.toCharArray())
    }

    //해시 검증
    fun verifyHash(cipherText: String, hash: String): Boolean {
        return BCrypt.verifyer().verify(cipherText.toCharArray(), hash).verified
    }
}