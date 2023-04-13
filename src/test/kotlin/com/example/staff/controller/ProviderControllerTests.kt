package com.example.staff.controller

import com.example.staff.model.GroupEntity
import com.example.staff.model.MethodPermissionEntity
import com.example.staff.model.ProviderEntity
import com.example.staff.model.UserEntity
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class ProviderControllerTests {

    @SpringBootTest
    @AutoConfigureMockMvc
    class ProviderControllerGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.PROVIDER
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(get("/provider").header("authorization", token))
                ?.andExpect(status().isOk)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class ProviderControllerPostTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.PROVIDER
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should post item`() {
            val token = transaction {
                ProviderEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/provider")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "EMAIL"}""")
                )
                ?.andExpect(status().isOk)
        }

        @Test
        fun `Shouldn't post without token`() {
            transaction {
                ProviderEntity.deleteAll()
            }

            mockMvc
                ?.perform(
                    post("/provider")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "EMAIL"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't post without method permission`() {
            val token = transaction {
                ProviderEntity.deleteAll()
                GroupEntity.deleteAll()
                GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

                accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
            }

            mockMvc
                ?.perform(
                    post("/provider")
                        .header("authorization", token)
                        .header("Content-Type", "application/json")
                        .content("""{"id": "EMAIL"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class ProviderControllerPutTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.PROVIDER
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should update item`() {
            val token = transaction {
                ProviderEntity.deleteAll()

                addPermission().also {
                    ProviderEntity.insert {
                        it[id] = EntityID("email", ProviderEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/provider")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "email"}""")
                )
                ?.andExpect(status().isOk)
        }


        @Test
        fun `Should update without token`() {
            transaction {
                ProviderEntity.deleteAll()

                ProviderEntity.insert {
                    it[id] = EntityID("email", ProviderEntity)
                }
            }

            mockMvc
                ?.perform(
                    put("/provider")
                        .header("Content-Type", "application/json")
                        .content("""{"id": "email"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class ProviderControllerDeleteTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.PROVIDER
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                ProviderEntity.deleteAll()

                addPermission().also {
                    ProviderEntity.insert {
                        it[id] = EntityID("email", ProviderEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/provider?id=email")
                        .header("Content-Type", "application/json")
                        .header("authorization", token)
                        .content("""{"id": "email"}""")
                )
                ?.andExpect(status().isOk)
        }
    }
}
