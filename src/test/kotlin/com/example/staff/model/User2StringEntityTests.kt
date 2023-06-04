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
class User2StringEntityTests {
    @Test
    fun `Should create instance`() {
        transaction {
            UserEntity.deleteAll()
            PropertyEntity.deleteAll()

            User2StringEntity.insert {
                it[user] = UserEntity.insertAndGetId {
                    it[id] = EntityID(1, UserEntity)
                    it[login] = "user_name"
                }
                it[property] = PropertyEntity.insertAndGetId {
                    it[id] = EntityID("name", PropertyEntity)
                    it[type] = PropertyType.STRING
                }
                it[value] = "VALUE"
            }.resultedValues?.firstOrNull()?.let {
                Assertions.assertEquals(1, it[User2StringEntity.user].value)
                Assertions.assertEquals("name", it[User2StringEntity.property].value)
                Assertions.assertEquals("VALUE", it[User2StringEntity.value])
            }
        }
    }
}