package com.example.staff.controller

import com.example.staff.model.*
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
import com.example.staff.resolver.PropertyResolver
import com.example.staff.resolver.StatusResolver
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class StatusControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class StatusControllerGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.STATUS
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                StatusEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(get("/status").header("authorization", token))
                ?.andExpect(status().isOk)
        }

        @Test
        fun `Should get full list`() {
            val token = transaction {
                StatusEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        StatusEntity.insert {
                            it[id] = EntityID("status_${i}", StatusEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/status").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<StatusResolver>::class.java)
                    Assertions.assertEquals(10, list.size)
                }
        }

        @Test
        fun `Should get list with limit`() {
            val token = transaction {
                StatusEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        StatusEntity.insert {
                            it[id] = EntityID("status_${i}", StatusEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/status?limit=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PropertyResolver>::class.java)
                    Assertions.assertEquals(5, list.size)
                }
        }

        @Test
        fun `Should get list with offset`() {
            val token = transaction {
                StatusEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        StatusEntity.insert {
                            it[id] = EntityID("status_${i}", StatusEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/status?offset=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PropertyResolver>::class.java)
                    Assertions.assertEquals(5, list.size)
                    Assertions.assertEquals("status_6", list.first().id)
                }
        }

        @Test
        fun `Shouldn't get without token`() {
            transaction {
                StatusEntity.deleteAll()
            }

            mockMvc
                ?.perform(get("/status"))
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't get without method permission`() {
            val token = transaction {
                StatusEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(get("/status").header("authorization", token))
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class StatusControllerPostTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.STATUS
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should add item`() {
            val token = transaction {
                StatusEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/status")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "active"}""")
                )
                ?.andExpect(status().isOk)
        }

        @Test
        fun `Shouldn't add without id`() {
            val token = transaction {
                StatusEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/status")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add with blank id`() {
            val token = transaction {
                StatusEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/status")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add without token`() {
            transaction {
                StatusEntity.deleteAll()
            }

            mockMvc
                ?.perform(
                    post("/status")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "ACTIVE"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't add without method permission`() {
            val token = transaction {
                StatusEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    post("/status")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "ACTIVE"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }


    @SpringBootTest
    @AutoConfigureMockMvc
    class StatusControllerPutTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.STATUS
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                StatusEntity.deleteAll()

                addPermission().also {
                    StatusEntity.insert {
                        it[id] = EntityID("active", StatusEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/status")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "active"}""")
                )
                ?.andExpect(status().isOk)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class StatusControllerDeleteTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.STATUS
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should delete item`() {
            val token = transaction {
                StatusEntity.deleteAll()

                addPermission().also {
                    StatusEntity.insert {
                        it[id] = EntityID("active", PropertyEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/status?id=active")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
        }
    }
}