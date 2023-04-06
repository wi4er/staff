package com.example.staff.controller

import com.example.staff.model.GroupEntity
import com.example.staff.model.MethodPermissionEntity
import com.example.staff.model.UserEntity
import com.example.staff.model.UserPermissionEntity
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
import com.example.staff.resolver.PermissionResolver
import com.example.staff.resolver.UserResolver
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SchemaUtils
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

class PermissionControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class PermissionControllerGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.PERMISSION
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                MethodPermissionEntity.deleteAll()

                SchemaUtils.create(MethodPermissionEntity)
                addPermission()
            }

            mockMvc
                ?.perform(get("/permission").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<PermissionResolver>::class.java)
                    Assertions.assertEquals(1, list.size)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class PermissionControllerPostTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.PERMISSION
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get post item`() {
            val token = transaction {
                MethodPermissionEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/permission")
                        .header("authorization", token)
                        .contentType("application/json")
                        .content("""{"method": "GET", "entity": "GROUP", "group": 777}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, PermissionResolver::class.java)

                    Assertions.assertEquals(MethodType.GET, item.method)
                    Assertions.assertEquals(EntityType.GROUP, item.entity)
                    Assertions.assertEquals(777, item.group)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class PermissionControllerPutTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.PERMISSION
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get post item`() {
            val token = transaction {
                MethodPermissionEntity.deleteAll()
                addPermission().also {
                    MethodPermissionEntity.insert {
                        it[id] = EntityID(1, MethodPermissionEntity)
                        it[method] = MethodType.GET
                        it[entity] = EntityType.GROUP
                        it[group] = EntityID(777, GroupEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/permission")
                        .header("authorization", token)
                        .contentType("application/json")
                        .content("""{"id": 1, "method": "POST", "entity": "USER", "group": 777}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, PermissionResolver::class.java)

                    Assertions.assertEquals(MethodType.POST, item.method)
                    Assertions.assertEquals(EntityType.USER, item.entity)
                    Assertions.assertEquals(777, item.group)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerDeleteTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.PERMISSION
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should delete item`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    MethodPermissionEntity.insert {
                        it[id] = EntityID(1, MethodPermissionEntity)
                        it[method] = MethodType.GET
                        it[entity] = EntityType.GROUP
                        it[group] = EntityID(777, GroupEntity)
                    }
                }
            }

            mockMvc
                ?.perform(delete("/permission?id=1").header("authorization", token))
                ?.andExpect(status().isOk)
        }
    }
}