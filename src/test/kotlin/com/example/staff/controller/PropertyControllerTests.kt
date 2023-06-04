package com.example.staff.controller

import com.example.staff.model.*
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
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

class PropertyControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class PropertyControllerGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()
            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.PROPERTY
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(get("/property").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PropertyResolver>::class.java)
                    assertEquals(0, list.size)
                }
        }

        @Test
        fun `Should get full list`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        PropertyEntity.insert {
                            it[id] = EntityID("name_${i}", StatusEntity)
                            it[type] = PropertyType.STRING
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/property").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PropertyResolver>::class.java)
                    assertEquals(10, list.size)
                }
        }

        @Test
        fun `Should get by id`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        PropertyEntity.insert {
                            it[id] = EntityID("name_${i}", StatusEntity)
                            it[type] = PropertyType.STRING
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/property?filter=name-eq-name_5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PropertyResolver>::class.java)
                    assertEquals(1, list.size)
                    assertEquals("name_5", list.firstOrNull()?.id)
                }
        }

        @Test
        fun `Should get list with limit`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        PropertyEntity.insert {
                            it[id] = EntityID("property_${i}", PropertyEntity)
                            it[type] = PropertyType.STRING
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/property?limit=5").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PropertyResolver>::class.java)
                    assertEquals(5, list.size)
                    assertEquals("property_5", list.last().id)
                }
        }

        @Test
        fun `Should get list with offset`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission().also {
                    for (i in 1..10) {
                        PropertyEntity.insert {
                            it[id] = EntityID("status_${i}", PropertyEntity)
                            it[type] = PropertyType.STRING
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/property?offset=5").header("authorization", token))
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
                PropertyEntity.deleteAll()
            }

            mockMvc
                ?.perform(get("/property"))
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
                ?.perform(get("/property").header("authorization", token))
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class PropertyControllerPostTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.PROPERTY
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should add item`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/property")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "NAME", "type": "STRING"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect(jsonPath("$.id").value("NAME"))
                ?.andExpect(jsonPath("$.type").value("STRING"))
        }

        @Test
        fun `Shouldn't add without id`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/property")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"type": "STRING"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add without type`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/property")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "NAME"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add with wrong type`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/property")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "NAME", "type": "WRONG"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add with blank id`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/property")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "", "type": "STRING"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add without token`() {
            transaction {
                PropertyEntity.deleteAll()
            }

            mockMvc
                ?.perform(
                    post("/property")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "EMAIL", "type": "STRING"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't add without method permission`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    post("/property")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "EMAIL", "type": "STRING"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class PropertyControllerPutTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.PROPERTY
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should update item`() {
            val token = transaction {
                PropertyEntity.deleteAll()

                addPermission().also {
                    PropertyEntity.insert {
                        it[id] = EntityID("name", ProviderEntity)
                        it[type] = PropertyType.STRING
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/property?id=name")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "change", "type": "USER"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect(jsonPath("$.id").value("change"))
                ?.andExpect(jsonPath("$.type").value("USER"))
        }

        @Test
        fun `Shouldn't update with wrong id`() {
            val token = transaction {
                PropertyEntity.deleteAll()

                addPermission().also {
                    PropertyEntity.insert {
                        it[id] = EntityID("name", ProviderEntity)
                        it[type] = PropertyType.STRING
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/property?id=wrong")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "wrong", "type": "STRING"}""")
                )
                ?.andExpect(status().isNotFound)
        }

        @Test
        fun `Shouldn't update to blank id`() {
            val token = transaction {
                PropertyEntity.deleteAll()

                addPermission().also {
                    PropertyEntity.insert {
                        it[id] = EntityID("name", ProviderEntity)
                        it[type] = PropertyType.STRING
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/property?id=name")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "", "type": "STRING"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Should update without token`() {
            transaction {
                PropertyEntity.deleteAll()

                PropertyEntity.insert {
                    it[id] = EntityID("name", PropertyEntity)
                    it[type] = PropertyType.STRING
                }
            }

            mockMvc
                ?.perform(
                    put("/property?id=name")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "name", "type": "STRING"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't update without method permission`() {
            val token = transaction {
                GroupEntity.deleteAll()
                PropertyEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                PropertyEntity.insert {
                    it[id] = EntityID("name", PropertyEntity)
                    it[type] = PropertyType.STRING
                }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    put("/property?id=name")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "name", "type": "STRING"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class PropertyControllerDeleteTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.PROPERTY
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should delete item`() {
            val token = transaction {
                PropertyEntity.deleteAll()

                addPermission().also {
                    PropertyEntity.insert {
                        it[id] = EntityID("name", PropertyEntity)
                        it[type] = PropertyType.STRING
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/property?id=name")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
        }

        @Test
        fun `Shouldn't delete without token`() {
            transaction {
                PropertyEntity.deleteAll()

                PropertyEntity.insert {
                    it[id] = EntityID("name", PropertyEntity)
                    it[type] = PropertyType.STRING
                }
            }

            mockMvc
                ?.perform(delete("/property?id=name"))
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't delete without method permission`() {
            val token = transaction {
                PropertyEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                PropertyEntity.insert {
                    it[id] = EntityID("name", PropertyEntity)
                    it[type] = PropertyType.STRING
                }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    delete("/property?id=name")
                        .header("authorization", token)
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't delete with wrong id`() {
            val token = transaction {
                PropertyEntity.deleteAll()

                addPermission().also {
                    PropertyEntity.insert {
                        it[id] = EntityID("name", PropertyEntity)
                        it[type] = PropertyType.STRING
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/property?id=wrong")
                        .header("authorization", token)
                )
                ?.andExpect(status().isNotFound)
        }
    }
}
