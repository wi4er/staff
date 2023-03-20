package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class User2GroupEntityTests {
    @Test
    fun `Should get empty list`() {
        transaction {
            User2GroupEntity.deleteAll()

            val list = User2GroupEntity.selectAll().toList()

            Assertions.assertEquals(0, list.size)
        }
    }

    @Test
    fun `Shouldn't create same value`() {
        transaction {
            GroupEntity.deleteAll()
            UserEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(1, GroupEntity) }
            UserEntity.insert {
                it[login] = "user_name"
                it[id] = EntityID(1, UserEntity)
            }

            User2GroupEntity.insert {
                it[user] = EntityID(1, UserEntity)
                it[group] = EntityID(1, GroupEntity)
            }

            assertThrows<ExposedSQLException> {
                User2GroupEntity.insert {
                    it[user] = EntityID(1, UserEntity)
                    it[group] = EntityID(1, GroupEntity)
                }
            }
        }
    }
}