package com.example.commerce.auth

import com.example.commerce.auth.util.HashUtil
import com.example.commerce.auth.util.JwtUtil
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AuthService(private val database: Database) {

    //에러 로그 확인을 위해
    private val logger = LoggerFactory.getLogger(this.javaClass.name)

    fun createIdentity(req: SignupRequest) : Long {
        //계정 중복 확인
        val record = transaction {
            Identities.select { Identities.userid eq req.userid }.singleOrNull()
        }
        if(record != null) {
            return 0;
        }
        val secret = HashUtil.createHash(req.password)

        val profileId = transaction {
            try{
                val identityId = Identities.insertAndGetId {
                    it[this.userid] = req.userid
                    it[this.secret] = secret
                }

                val profileId = Profiles.insertAndGetId {
                    it[this.nickname] = req.nickname
                    it[this.email] = req.email
                    it[this.phone] = req.phone
                    it[this.identityId] = identityId.value
                }
                return@transaction profileId.value
            }catch (e: Exception){
                rollback()
                //에러메세지 확인
                logger.error(e.message)
                return@transaction 0
            }
        }
        return profileId
    }

    fun authenticate(userid: String, password: String) : Pair<Boolean, String> {
        val (result, payload) = transaction(database.transactionManager.defaultIsolationLevel, readOnly = true) {
            val i = Identities
            val p = Profiles
            //인증정보 조회
            val identityRecord = i.select{i.userid eq userid}.singleOrNull()
                ?: return@transaction Pair(false, mapOf("message" to "Unauthorized"))
            //프로필 정보조회
            val profileRecord = p.select{p.identityId eq identityRecord[i.id].value}.singleOrNull()
                ?: return@transaction Pair(false, mapOf("message" to "Conflict"))

            return@transaction Pair(true, mapOf(
                "id" to profileRecord[p.id],
                "nickname" to profileRecord[p.nickname],
                "userid" to identityRecord[i.userid],
                "secret" to identityRecord[i.secret]
            ))
        }

        if(!result) {
            return Pair(false, payload["message"].toString())
        }

        val isVerified = HashUtil.verifyHash(password, payload["secret"].toString())
        if (!isVerified) {
            return Pair(false, "Unauthorized")
        }
        val token = JwtUtil.createToken(
            payload["id"].toString().toLong(),
            payload["userid"].toString(),
            payload["nickname"].toString()
        )
        return Pair(true, token)
    }
}