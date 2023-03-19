package com.example.staff.model

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GroupEntityTests {
    @Test
    fun `Should get empty list`() {
        transaction {
            GroupEntity.deleteAll()

            addLogger(StdOutSqlLogger)

            val list = GroupEntity.selectAll().toList()

            Assertions.assertEquals(0, list.size)
        }
    }

    @Test
    fun `Should create group with users`() {
        val user_group = transaction {
            GroupEntity.deleteAll()

            Group.new {}
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