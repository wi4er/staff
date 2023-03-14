package com.example.staff.model

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserEntityTests {
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