package com.example.staff.model

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SizedCollection
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserGroupEntityTests {
    @Test
    fun `Should create group`() {
        transaction {
            SchemaUtils.create(UserGroupEntity)
            SchemaUtils.create(User2UserGroup)

        }
    }

    @Test
    fun `Should create group with users`() {
        val user_group = transaction {
            UserGroup.new {}
        }

        val user = transaction {
            UserEntity.deleteAll()

            User.new {
                login = "user_name"
            }
        }

        transaction {
            user.group = SizedCollection(listOf(user_group))
        }
    }
}