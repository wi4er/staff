package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ContactEntityTests {
    @Test
    fun `Should get empty list`() {
        transaction {
            ContactEntity.deleteAll()

            val list = ContactEntity.selectAll().toList()
            Assertions.assertEquals(0, list.size)
        }
    }

    @Test
    fun `Should add item`() {
        transaction {
            ContactEntity.deleteAll()

            val inst = ContactEntity.insert {
                it[id] = EntityID("EMAIL", ContactEntity)
                it[type] = ContactType.EMAIL
            }.resultedValues?.firstOrNull()

            Assertions.assertEquals("EMAIL", inst?.get(ContactEntity.id)?.value)
            Assertions.assertEquals(ContactType.EMAIL, inst?.get(ContactEntity.type))
            println(inst)
        }
    }

}