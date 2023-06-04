package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class User2PointEntityTests {
    @Test
    fun `Should create instance`() {
        transaction {
            UserEntity.deleteAll()
            PropertyEntity.deleteAll()
            DirectoryEntity.deleteAll()

            SchemaUtils.create(User2PointEntity)

            User2PointEntity.insert {
                it[user] = UserEntity.insertAndGetId {
                    it[id] = EntityID(1, UserEntity)
                    it[login] = "user_name"
                }
                it[point] = PointEntity.insertAndGetId {
                    it[directory] = DirectoryEntity.insertAndGetId { it[id] = EntityID("city", DirectoryEntity) }
                    it[id] = EntityID("London", PointEntity)
                }
                it[property] = PropertyEntity.insertAndGetId {
                    it[id] = EntityID("name", PropertyEntity)
                    it[type] = PropertyType.POINT
                }
            }.resultedValues?.firstOrNull()?.let {
                Assertions.assertEquals(1, it[User2PointEntity.user].value)
                Assertions.assertEquals("London", it[User2PointEntity.point].value)
                Assertions.assertEquals("name", it[User2PointEntity.property].value)
            }
        }
    }

    @Test
    fun `Shouldn't create same point`() {
        transaction {
            UserEntity.deleteAll()
            PropertyEntity.deleteAll()
            DirectoryEntity.deleteAll()

            SchemaUtils.create(User2PointEntity)

            val userId = UserEntity.insertAndGetId {
                it[id] = EntityID(1, UserEntity)
                it[login] = "user_name"
            }
            val pointId = PointEntity.insertAndGetId {
                it[directory] = DirectoryEntity.insertAndGetId { it[id] = EntityID("city", DirectoryEntity) }
                it[id] = EntityID("London", PointEntity)
            }
            val propertyId = PropertyEntity.insertAndGetId {
                it[id] = EntityID("name", PropertyEntity)
                it[type] = PropertyType.POINT
            }
            User2PointEntity.insert {
                it[user] = userId
                it[point] = pointId
                it[property] = propertyId
            }

            assertThrows<ExposedSQLException> {
                User2PointEntity.insert {
                    it[user] = userId
                    it[point] = pointId
                    it[property] = propertyId
                }
            }
        }
    }
}