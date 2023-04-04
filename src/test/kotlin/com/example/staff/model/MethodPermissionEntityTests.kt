package com.example.staff.model

import com.example.staff.permission.EntityType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MethodPermissionEntityTests {

    @Test
    fun `Should create permission`() {
        transaction {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(1, GroupEntity) }
            val inst = MethodPermissionEntity.insert {
                it[entity] = EntityType.USER
                it[method] = MethodType.GET
                it[group] = EntityID(1, GroupEntity)
            }

            inst.resultedValues?.get(0)?.let {
                Assertions.assertEquals(MethodType.GET, it[MethodPermissionEntity.method])
            }
        }
    }
}