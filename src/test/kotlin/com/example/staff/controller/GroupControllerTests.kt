package com.example.staff.controller

import com.example.staff.model.GroupEntity
import com.example.staff.model.MethodPermissionEntity
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
import com.example.staff.resolver.GroupResolver
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
class GroupControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class GroupControllerGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.GROUP
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                GroupEntity.deleteAll()

                addPermission()
            }

            mockMvc
                ?.perform(get("/group").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<GroupResolver>::class.java)
                    Assertions.assertEquals(1, list.size)
                }
        }

        @Test
        fun `Shouldn't get without authorization`() {
            transaction {
                GroupEntity.deleteAll()
            }

            mockMvc
                ?.perform(get("/group"))
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class GroupControllerPostTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.GROUP
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should post item`() {
            val token = transaction {
                GroupEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":22}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)
                    Assertions.assertEquals(22, item.id)
                }
        }

        @Test
        fun `Should post with parent`() {
            val token = transaction {
                GroupEntity.deleteAll()
                addPermission().also {
                    GroupEntity.insert { it[id] = EntityID(111, GroupEntity) }
                }
            }

            mockMvc
                ?.perform(
                    post("/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "parent": 111}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)
                    Assertions.assertEquals(111, item.parent)
                }
        }

        @Test
        fun `Shouldn't post with wrong parent`() {
            val token = transaction {
                GroupEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "parent": 4444}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't post with duplicate id`() {
            val token = transaction {
                GroupEntity.deleteAll()

                addPermission().also {
                    GroupEntity.insert {
                        it[GroupEntity.id] = EntityID(1, GroupEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    post("/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Should post without id`() {
            val token = transaction {
                GroupEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)
                    Assertions.assertNotNull(item.id)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class GroupControllerPutTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.GROUP
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should add parent to item`() {
            val token = transaction {
                addPermission().also {
                    GroupEntity.insert { it[id] = EntityID(1, GroupEntity) }
                    GroupEntity.insert { it[id] = EntityID(111, GroupEntity) }
                }
            }

            mockMvc
                ?.perform(
                    put("/group?id=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "parent": 111}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)

                    Assertions.assertEquals(111, item.parent)
                }
        }

        @Test
        fun `Should remove parent from item`() {
            val token = transaction {
                addPermission().also {
                    GroupEntity.insert { it[id] = EntityID(111, GroupEntity) }
                    GroupEntity.insert {
                        it[id] = EntityID(1, GroupEntity)
                        it[parent] = EntityID(111, GroupEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/group?id=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "parent": null}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)

                    Assertions.assertNull(item.parent)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class GroupControllerDeleteTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.GROUP
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should delete item`() {
            val token = transaction {
                addPermission().also {
                    GroupEntity.insert { it[id] = EntityID(123, GroupEntity) }
                }
            }

            mockMvc?.perform(
                delete("/group?id=123")
                    .header("authorization", token)
            )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)
                    Assertions.assertEquals(123, item.id)
                }
        }

        @Test
        fun `Shouldn't delete with wrong id`() {
            transaction { GroupEntity.deleteAll() }

            mockMvc
                ?.perform(delete("/group?id=77"))
                ?.andExpect(status().isForbidden)
        }
    }
}