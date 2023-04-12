package com.example.staff.model

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PropertyEntityTests {
    @Test
    fun `Should get empty list`() {
        transaction {
            PropertyEntity.deleteAll()

            val list = PropertyEntity.selectAll().toList()
            assertEquals(0, list.size)
        }
    }
}