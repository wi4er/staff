package com.example.staff.controller

import com.example.staff.model.*
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
import com.example.staff.resolver.DirectoryResolver
import com.example.staff.resolver.PropertyResolver
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

class DirectoryControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class DirectoryControllerGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.DIRECTORY
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(get("/directory").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<DirectoryResolver>::class.java)
                    Assertions.assertEquals(0, list.size)
                }
        }

        @Test
        fun `Should get full list`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        DirectoryEntity.insert {
                            it[id] = EntityID("status_${i}", StatusEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/directory").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PropertyResolver>::class.java)
                    Assertions.assertEquals(10, list.size)
                }
        }

        @Test
        fun `Should get list with limit`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        DirectoryEntity.insert {
                            it[id] = EntityID("property_${i}", DirectoryEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/directory?limit=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<DirectoryResolver>::class.java)
                    Assertions.assertEquals(5, list.size)
                    Assertions.assertEquals("property_5", list.last().id)
                }
        }

        @Test
        fun `Should get list with offset`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        DirectoryEntity.insert {
                            it[id] = EntityID("dir_${i}", DirectoryEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/directory?offset=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<DirectoryResolver>::class.java)
                    Assertions.assertEquals(5, list.size)
                    Assertions.assertEquals("dir_6", list.first().id)
                }
        }

        @Test
        fun `Shouldn't get without token`() {
            transaction {
                PropertyEntity.deleteAll()
            }

            mockMvc
                ?.perform(get("/directory"))
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't get without method permission`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(get("/directory").header("authorization", token))
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class DirectoryControllerPostTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.DIRECTORY
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should add item`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/directory")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "CITY"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, DirectoryResolver::class.java)
                    Assertions.assertEquals("CITY", list.id)
                }
        }

        @Test
        fun `Shouldn't add without id`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/directory")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add with blank id`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/directory")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add without token`() {
            transaction {
                DirectoryEntity.deleteAll()
            }

            mockMvc
                ?.perform(
                    post("/directory")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "CITY"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't add without method permission`() {
            val token = transaction {
                DirectoryEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    post("/directory")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "CITY"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }
}