package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class User2UserEntityTests {
    @Test
    fun `Should create instance`() {
        transaction {
            UserEntity.deleteAll()
            PropertyEntity.deleteAll()

            val childId = UserEntity.insertAndGetId {
                it[id] = EntityID(1, UserEntity)
                it[login] = "child_name"
            }
            val userId = UserEntity.insertAndGetId {
                it[id] = EntityID(2, UserEntity)
                it[login] = "user_name"
            }
            val propertyId = PropertyEntity.insertAndGetId {
                it[id] = EntityID("child", PropertyEntity)
                it[type] = PropertyType.USER
            }

            User2UserEntity.insert {
                it[user] = userId
                it[child] = childId
                it[property] = propertyId
            }
        }
    }
}