package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class GroupEntityTests {
    @Test
    fun `Should get empty list`() {
        transaction {
            GroupEntity.deleteAll()

            val list = GroupEntity.selectAll().toList()

            Assertions.assertEquals(0, list.size)
        }
    }

    @Test
    fun `Should create group`() {
        val id: EntityID<Int> = transaction {
            GroupEntity.deleteAll()
            GroupEntity.insertAndGetId { it[GroupEntity.id] = EntityID(1, GroupEntity) }
        }

        Assertions.assertEquals(1, id.value)
    }

    @Test
    fun `Should create with parent`() {
        transaction {
            GroupEntity.deleteAll()
            GroupEntity.insertAndGetId { it[id] = EntityID(1, GroupEntity) }

            GroupEntity.insertAndGetId {
                it[id] = EntityID(66, GroupEntity)
                it[parent] = EntityID(1, GroupEntity)
            }
        }

        transaction {
            val item = GroupEntity.select { GroupEntity.id eq 66 }
                .firstOrNull()

            Assertions.assertEquals(66, item?.get(GroupEntity.id)?.value)
            Assertions.assertEquals(1, item?.get(GroupEntity.parent)?.value)
        }
    }
}