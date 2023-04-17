package com.example.staff.controller

import com.example.staff.model.*
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
import com.example.staff.resolver.PointResolver
import com.example.staff.resolver.PropertyResolver
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class PointControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class PointControllerGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.POINT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                PointEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(get("/point").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PointResolver>::class.java)
                    assertEquals(0, list.size)
                }
        }

        @Test
        fun `Should get full list`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                addPermission().also {
                    val dir = DirectoryEntity.insertAndGetId {
                        it[id] = EntityID("city", DirectoryEntity)
                    }

                    for (i in 1..10) {
                        PointEntity.insert {
                            it[id] = EntityID("street_${i}", StatusEntity)
                            it[directory] = dir
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/point").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PointResolver>::class.java)
                    assertEquals(10, list.size)
                }
        }

        @Test
        fun `Should get list with limit`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                val dir = DirectoryEntity.insertAndGetId {
                    it[id] = EntityID("city", DirectoryEntity)
                }

                addPermission().also {
                    for (i in 1..10) {
                        PointEntity.insert {
                            it[id] = EntityID("street_${i}", PropertyEntity)
                            it[directory] = dir
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/point?limit=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PointResolver>::class.java)
                    assertEquals(5, list.size)
                    assertEquals("street_5", list.last().id)
                }
        }

        @Test
        fun `Should get list with offset`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                val dir = DirectoryEntity.insertAndGetId {
                    it[id] = EntityID("city", DirectoryEntity)
                }

                addPermission().also {
                    for (i in 1..10) {
                        PointEntity.insert {
                            it[id] = EntityID("status_${i}", PointEntity)
                            it[directory] = dir
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/point?offset=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PropertyResolver>::class.java)
                    assertEquals(5, list.size)
                    assertEquals("status_6", list.first().id)
                }
        }

        @Test
        fun `Shouldn't get without token`() {
            transaction {
                PointEntity.deleteAll()
            }

            mockMvc
                ?.perform(get("/point"))
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't get without method permission`() {
            val token = transaction {
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(get("/point").header("authorization", token))
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class PointControllerPostTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.POINT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should add item`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                DirectoryEntity.insertAndGetId {
                    it[id] = EntityID("city", DirectoryEntity)
                }

                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/point")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "NAME", "directory": "city"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, PointResolver::class.java)
                    assertEquals("NAME", item.id)
                    assertEquals("city", item.directory)
                }
        }

        @Test
        fun `Shouldn't add without id`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                DirectoryEntity.insertAndGetId {
                    it[id] = EntityID("city", DirectoryEntity)
                }

                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/point")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"directory": "city"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add without directory`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                DirectoryEntity.insertAndGetId {
                    it[id] = EntityID("city", DirectoryEntity)
                }

                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/point")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "street"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add with blank id`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                DirectoryEntity.insertAndGetId {
                    it[id] = EntityID("city", DirectoryEntity)
                }

                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/point")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "", "directory": "city"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add without token`() {
            transaction {
                PointEntity.deleteAll()
            }

            mockMvc
                ?.perform(
                    post("/point")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "city"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't add without method permission`() {
            val token = transaction {
                PointEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    post("/point")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "city"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class PointControllerPutTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.POINT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should update item`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                addPermission().also {
                    val dir = DirectoryEntity.insertAndGetId {
                        it[id] = EntityID("city", DirectoryEntity)
                    }

                    PointEntity.insert {
                        it[id] = EntityID("name", ProviderEntity)
                        it[directory] = dir
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/point?id=name")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "change", "directory": "city"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, PointResolver::class.java)
                    assertEquals("change", item.id)
                }
        }

        @Test
        fun `Shouldn't update with wrong id`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                addPermission().also {
                    val dir = DirectoryEntity.insertAndGetId {
                        it[id] = EntityID("city", DirectoryEntity)
                    }

                    PointEntity.insert {
                        it[id] = EntityID("name", PointEntity)
                        it[directory] = dir
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/point?id=wrong")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "wrong", "directory": "city"}""")
                )
                ?.andExpect(status().isNotFound)
        }

        @Test
        fun `Shouldn't update to blank id`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                addPermission().also {
                    val dir = DirectoryEntity.insertAndGetId {
                        it[id] = EntityID("city", DirectoryEntity)
                    }

                    PointEntity.insert {
                        it[id] = EntityID("name", ProviderEntity)
                        it[directory] = dir
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/point?id=name")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "", "directory":"city"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Should update without token`() {
            transaction {
                DirectoryEntity.deleteAll()

                val dir = DirectoryEntity.insertAndGetId {
                    it[id] = EntityID("city", DirectoryEntity)
                }

                PointEntity.insert {
                    it[id] = EntityID("london", PointEntity)
                    it[directory] = dir
                }
            }

            mockMvc
                ?.perform(
                    put("/point?id=london")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "london"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't update without method permission`() {
            val token = transaction {
                GroupEntity.deleteAll()
                DirectoryEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                val dir = DirectoryEntity.insertAndGetId {
                    it[id] = EntityID("city", DirectoryEntity)
                }

                PointEntity.insert {
                    it[id] = EntityID("name", PropertyEntity)
                    it[directory] = dir
                }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    put("/point?id=name")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "name"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class PointControllerDeleteTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.POINT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should delete item`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                addPermission().also {
                    val dir = DirectoryEntity.insertAndGetId {
                        it[id] = EntityID("city", DirectoryEntity)
                    }

                    PointEntity.insert {
                        it[id] = EntityID("name", PointEntity)
                        it[directory] = dir
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/point?id=name")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
        }

        @Test
        fun `Shouldn't delete without token`() {
            mockMvc
                ?.perform(delete("/point?id=name"))
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't delete without method permission`() {
            val token = transaction {
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    delete("/point?id=name")
                        .header("authorization", token)
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't delete with wrong id`() {
            val token = transaction {
                DirectoryEntity.deleteAll()

                addPermission().also {
                    val dir = DirectoryEntity.insertAndGetId {
                        it[id] = EntityID("city", DirectoryEntity)
                    }

                    PointEntity.insert {
                        it[id] = EntityID("name", PointEntity)
                        it[directory] = dir
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/point?id=wrong")
                        .header("authorization", token)
                )
                ?.andExpect(status().isNotFound)
        }
    }
}
