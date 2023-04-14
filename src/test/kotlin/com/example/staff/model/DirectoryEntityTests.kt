package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class DirectoryEntityTests {
    @Test
    fun `Should create item`() {
        transaction {
            DirectoryEntity.deleteAll()

            DirectoryEntity.insert {
                it[id] = EntityID("city", DirectoryEntity)
            }.resultedValues?.firstOrNull()?.let {
                assertEquals("city", it[DirectoryEntity.id].value)
            }
        }
    }
}