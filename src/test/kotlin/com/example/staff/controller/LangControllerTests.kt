package com.example.staff.controller

import com.example.staff.model.*
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
import com.example.staff.resolver.LangResolver
import com.example.staff.resolver.PropertyResolver
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class LangControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class LangControllerGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.LANG
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                LangEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(get("/lang").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<LangResolver>::class.java)
                    assertEquals(0, list.size)
                }
        }

        @Test
        fun `Should get full list`() {
            val token = transaction {
                LangEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        LangEntity.insert {
                            it[id] = EntityID("lang_${i}", LangEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/lang").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<LangResolver>::class.java)
                    assertEquals(10, list.size)
                }
        }

        @Test
        fun `Should get list with limit`() {
            val token = transaction {
                LangEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        LangEntity.insert {
                            it[id] = EntityID("lang_${i}", LangEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/lang?limit=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<LangResolver>::class.java)
                    assertEquals(5, list.size)
                    assertEquals("lang_5", list.last().id)
                }
        }

        @Test
        fun `Should get list with offset`() {
            val token = transaction {
                LangEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        LangEntity.insert {
                            it[id] = EntityID("lang_${i}", LangEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/lang?offset=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<LangResolver>::class.java)
                    assertEquals(5, list.size)
                    assertEquals("lang_6", list.first().id)
                }
        }

        @Test
        fun `Shouldn't get without token`() {
            mockMvc
                ?.perform(get("/lang"))
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't get without method permission`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(get("/lang").header("authorization", token))
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class LangControllerPostTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.LANG
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should add item`() {
            val token = transaction {
                LangEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/lang")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "GR"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, PropertyResolver::class.java)
                    assertEquals("GR", list.id)
                }
        }

        @Test
        fun `Shouldn't add without id`() {
            val token = transaction {
                LangEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/lang")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add with blank id`() {
            val token = transaction {
                LangEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/lang")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": ""}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add without token`() {
            mockMvc
                ?.perform(
                    post("/lang")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "GR"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't add without method permission`() {
            val token = transaction {
                LangEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    post("/lang")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "EMAIL"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class LangControllerPutTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.LANG
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should update item`() {
            val token = transaction {
                LangEntity.deleteAll()

                addPermission().also {
                    LangEntity.insert {
                        it[id] = EntityID("EN", LangEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/lang?id=EN")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "change"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, PropertyResolver::class.java)
                    assertEquals("change", item.id)
                }
        }

        @Test
        fun `Shouldn't update with wrong id`() {
            val token = transaction {
                LangEntity.deleteAll()

                addPermission().also {
                    LangEntity.insert {
                        it[id] = EntityID("EN", LangEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/lang?id=wrong")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "wrong"}""")
                )
                ?.andExpect(status().isNotFound)
        }

        @Test
        fun `Shouldn't update to blank id`() {
            val token = transaction {
                LangEntity.deleteAll()

                addPermission().also {
                    LangEntity.insert {
                        it[id] = EntityID("EN", LangEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/lang?id=EN")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": ""}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Should update without token`() {
            transaction {
                LangEntity.deleteAll()

                LangEntity.insert {
                    it[id] = EntityID("EN", LangEntity)
                }
            }

            mockMvc
                ?.perform(
                    put("/lang?id=EN")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "name"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't update without method permission`() {
            val token = transaction {
                GroupEntity.deleteAll()
                LangEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                LangEntity.insert {
                    it[id] = EntityID("EN", LangEntity)
                }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    put("/lang?id=EN")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "name"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class LangControllerDeleteTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.LANG
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should delete item`() {
            val token = transaction {
                LangEntity.deleteAll()

                addPermission().also {
                    LangEntity.insert {
                        it[id] = EntityID("EN", LangEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/lang?id=EN")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
        }

        @Test
        fun `Shouldn't delete without token`() {
            mockMvc
                ?.perform(delete("/lang?id=EN"))
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't delete without method permission`() {
            val token = transaction {
                LangEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                LangEntity.insert {
                    it[id] = EntityID("EN", LangEntity)
                }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    delete("/lang?id=EN")
                        .header("authorization", token)
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't delete with wrong id`() {
            val token = transaction {
                LangEntity.deleteAll()

                addPermission().also {
                    LangEntity.insert {
                        it[id] = EntityID("EN", LangEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/lang?id=wrong")
                        .header("authorization", token)
                )
                ?.andExpect(status().isNotFound)
        }
    }
}
