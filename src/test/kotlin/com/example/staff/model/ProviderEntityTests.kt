package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ProviderEntityTests {
    @Test
    fun `Should create item`() {
        transaction {
            val res = ProviderEntity.insert {
                it[id] = EntityID("email", ProviderEntity)
            }.resultedValues?.first()

            assertEquals("email", res?.get(ProviderEntity.id)?.value)
        }
    }
}