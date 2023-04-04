package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class User2ContactEntityTests {
    @Test
    fun `Should create item`() {
        transaction {
            UserEntity.deleteAll()
            ContactEntity.deleteAll()

            val userId = UserEntity.insertAndGetId {
                it[id] = EntityID(1, UserEntity)
                it[login] = "user_login"
            }

            val contactId = ContactEntity.insertAndGetId {
                it[id] = EntityID("email", ContactEntity)
                it[type] = ContactType.EMAIL
            }

            val rows = User2ContactEntity.insert {
                it[user] = userId
                it[contact] = contactId
                it[value] = "mail@mail.com"
            }.resultedValues

            Assertions.assertEquals("mail@mail.com", rows?.firstOrNull()?.get(User2ContactEntity.value))
            Assertions.assertEquals("email", rows?.firstOrNull()?.get(User2ContactEntity.contact)?.value)
        }
    }
}