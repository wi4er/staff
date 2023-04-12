package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class User2ProviderEntityTests {
    @Test
    fun `Should create instance`() {
        transaction {
            UserEntity.deleteAll()
            ProviderEntity.deleteAll()

            val userId =UserEntity.insertAndGetId {
                it[id] = EntityID(1, UserEntity)
                it[login] = "user_name"
            }

            val providerId = ProviderEntity.insertAndGetId {
                it[id] = EntityID("email", ProviderEntity)
            }

            User2ProviderEntity.insert {
                it[user] = userId
                it[provider] = providerId
                it[hash] = "123"
            }.resultedValues?.first()?.let {
                assertEquals(1, it[User2ProviderEntity.user].value)
                assertEquals("email", it[User2ProviderEntity.provider].value)
                assertEquals("123", it[User2ProviderEntity.hash])
            }
        }
    }
}