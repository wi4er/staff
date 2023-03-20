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

            addLogger(StdOutSqlLogger)

            val list = GroupEntity.selectAll().toList()

            Assertions.assertEquals(0, list.size)
        }
    }

}