package com.example.commerce.auth

import jakarta.annotation.PostConstruct
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.context.annotation.Configuration

object Identities : LongIdTable("identity"){
    val secret = varchar("secret", 200)
    val userid = varchar("userid", length = 100)
}

object Profiles : LongIdTable("profile") {
    val email = varchar("email", 200)
    val nickname = varchar("nickname", 100)
    val phone = varchar("phone", 15)
    val identityId = reference("identity_id", Identities)
}
@Configuration
class AuthTableSetup(private val database: Database) {
    @PostConstruct
    fun migrateSchema() {
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(Identities, Profiles)
        }
    }
}