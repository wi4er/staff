package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PropertyEntityTests {
    @Test
    fun `Should get empty list`() {
        transaction {
            PropertyEntity.deleteAll()

            val list = PropertyEntity.selectAll().toList()
            Assertions.assertEquals(0, list.size)
        }
    }

    @Test
    fun `Should add item`() {
        transaction {
            PropertyEntity.deleteAll()

            PropertyEntity.insert {
                it[id] = EntityID("name", PropertyEntity)
                it[type] = PropertyType.STRING
            }.resultedValues?.firstOrNull()?.let {
                Assertions.assertEquals("name", it[PropertyEntity.id].value)
                Assertions.assertEquals(PropertyType.STRING, it[PropertyEntity.type])
            } ?: Assertions.fail()
        }
    }

    @Test
    fun `Shouldn't add with blank id`() {
        transaction {
            PropertyEntity.deleteAll()

            assertThrows<ExposedSQLException> {
                PropertyEntity.insert {
                    it[id] = EntityID("", PropertyEntity)
                    it[type] = PropertyType.STRING
                }
            }
        }
    }

    @Test
    fun `Shouldn't add with short id`() {
        transaction {
            PropertyEntity.deleteAll()

            assertThrows<ExposedSQLException> {
                PropertyEntity.insert {
                    it[id] = EntityID("123", PropertyEntity)
                    it[type] = PropertyType.STRING
                }
            }
        }
    }
}