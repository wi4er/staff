package com.example.staff.controller

import com.example.staff.model.*
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.UserAccount
import com.example.staff.resolver.ContactResolver
import com.example.staff.resolver.GroupResolver
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
class ContactControllerTests {
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
                it[entity] = EntityType.CONTACT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get empty list`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(get("/contact").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<GroupResolver>::class.java)
                    Assertions.assertEquals(0, list.size)
                }
        }

        @Test
        fun `Should get full list`() {
            val token = transaction {
                ContactEntity.deleteAll()

                addPermission().also {
                    for (i in 1..10) {
                        val contactId = ContactEntity.insertAndGetId {
                            it[id] = EntityID("CONTACT_${i}", ContactEntity)
                            it[type] = ContactType.EMAIL
                        }

                        ContactPermissionEntity.insert {
                            it[contact] = contactId
                            it[group] = EntityID(777, GroupEntity)
                            it[method] = MethodType.GET
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/contact").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<ContactResolver>::class.java)
                    Assertions.assertEquals(10, list.size)
                    Assertions.assertEquals("CONTACT_1", list.firstOrNull()?.id)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class GroupControllerPermissionGetTests {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.CONTACT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Shouldn't get without method permission`() {
            val token = transaction {
                GroupEntity.deleteAll()
            }

            mockMvc
                ?.perform(get("/contact").header("authorization", token))
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
                it[entity] = EntityType.CONTACT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should add item`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/contact")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"email", "type": "EMAIL"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item: ContactResolver = Gson().fromJson(it.response.contentAsString, ContactResolver::class.java)

                    Assertions.assertEquals("email", item.id)
                    Assertions.assertEquals(ContactType.EMAIL, item.type)
                }
        }

        @Test
        fun `Shouldn't add without type`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/contact")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"email"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't add without token`() {
            transaction {
                ContactEntity.deleteAll()
                GroupEntity.deleteAll()
            }

            mockMvc
                ?.perform(
                    post("/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"email", "type": "EMAIL"}""")
                )
                ?.andExpect(status().isForbidden)
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
                it[entity] = EntityType.CONTACT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should update item`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission().also {
                    ContactEntity.insert {
                        it[id] = EntityID("SOME", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }
                    ContactPermissionEntity.insert {
                        it[group] = EntityID(777, GroupEntity)
                        it[contact] = EntityID("SOME", ContactEntity)
                        it[method] = MethodType.PUT
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/contact")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"SOME", "type": "PHONE"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item: ContactResolver = Gson().fromJson(it.response.contentAsString, ContactResolver::class.java)

                    Assertions.assertEquals("SOME", item.id)
                    Assertions.assertEquals(ContactType.PHONE, item.type)
                }
        }
        @Test
        fun `Shouldn't update without permission`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission().also {
                    ContactEntity.insert {
                        it[id] = EntityID("SOME", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/contact")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"SOME", "type": "PHONE"}""")
                )
                ?.andExpect(status().isForbidden)
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
                it[entity] = EntityType.CONTACT
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should delete item`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission().also {
                    ContactEntity.insert {
                        it[id] = EntityID("SOME", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }
                    ContactPermissionEntity.insert {
                        it[group] = EntityID(777, GroupEntity)
                        it[contact] = EntityID("SOME", ContactEntity)
                        it[method] = MethodType.DELETE
                    }
                }
            }

            mockMvc
                ?.perform(
                    delete("/contact?id=SOME")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"SOME", "type": "PHONE"}""")
                )
                ?.andExpect(status().isOk)
        }
    }
}