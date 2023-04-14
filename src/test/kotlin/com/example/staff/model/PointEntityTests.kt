package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PointEntityTests {
    @Test
    fun `Should create item`() {
        transaction {
            PointEntity.deleteAll()
            DirectoryEntity.deleteAll()

            val directoryId = DirectoryEntity.insertAndGetId {
                it[id] = EntityID("city", DirectoryEntity)
            }

            PointEntity.insert {
                it[id] = EntityID("London", PointEntity)
                it[directory] = directoryId
            }
        }
    }

    @Test
    fun `Shouldn't create without directory`() {
        transaction {
            PointEntity.deleteAll()

            assertThrows<ExposedSQLException> {
                PointEntity.insert { it[id] = EntityID("London", PointEntity) }
            }
        }
    }
}