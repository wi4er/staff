package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserEntityTests {
    @Test
    fun `Should get empty list`() {
        transaction {
            UserEntity.deleteAll()
            val list = UserEntity.selectAll().toList()
            Assertions.assertEquals(0, list.size)
        }
    }

    @Test
    fun `Should create user`() {
        transaction {
            UserEntity.deleteAll()

            val inst = UserEntity.insert {
                it[login] = "user_name"
            }

            Assertions.assertEquals("user_name", inst.get(UserEntity.login))
        }
    }

    @Test
    fun `Should create user with groups`() {
        transaction {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(1, GroupEntity) }

            UserEntity.deleteAll()
            UserEntity.insert {
                it[id] = EntityID(1, UserEntity)
                it[login] = "user_name"
            }

            User2GroupEntity.insert {
                it[user] = EntityID(1, UserEntity)
                it[group] = EntityID(1, GroupEntity)
            }

            addLogger(StdOutSqlLogger)

        }
    }

    @Test
    fun `Shouldn't create with blank login`() {
        transaction {
            UserEntity.deleteAll()

            Assertions.assertThrows(ExposedSQLException::class.java) {
                UserEntity.insert { it[login] = "" }
            }
        }
    }

    @Test
    fun `Shouldn't create with doubled login`() {
        transaction {
            UserEntity.deleteAll()

            UserEntity.insert { it[login] = "uniq_name" }

            Assertions.assertThrows(ExposedSQLException::class.java) {
                UserEntity.insert { it[login] = "uniq_name" }
            }
        }
    }
}