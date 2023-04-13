package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class StatusEntityTests {
    @Test
    fun `Should create item`() {
        transaction {
            StatusEntity.deleteAll()

            StatusEntity.insert {
                it[id] = EntityID("active", StatusEntity)
            }.resultedValues?.firstOrNull()?.let {
                Assertions.assertEquals("active", it[StatusEntity.id].value)
            }
        }
    }
}