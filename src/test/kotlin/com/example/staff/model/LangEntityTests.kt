package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class LangEntityTests {
    @Test
    fun `Should create instance`() {
        transaction {
            LangEntity.deleteAll()

            LangEntity.insert {
                it[id] = EntityID("EN", LangEntity)
            }.resultedValues?.firstOrNull()?.let {
                Assertions.assertEquals("EN", it[LangEntity.id].value)
            }
        }
    }

    @Test
    fun `Shouldn't create with blank id`() {
        transaction {
            LangEntity.deleteAll()

            assertThrows<ExposedSQLException> {
                LangEntity.insert {
                    it[id] = EntityID("", LangEntity)
                }
            }
        }
    }

    @Test
    fun `Shouldn't create with short id`() {
        transaction {
            LangEntity.deleteAll()

            assertThrows<ExposedSQLException> {
                LangEntity.insert {
                    it[id] = EntityID("1", LangEntity)
                }
            }
        }
    }
}