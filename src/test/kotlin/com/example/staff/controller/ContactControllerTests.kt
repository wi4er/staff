package com.example.staff.controller

import com.example.staff.model.*
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
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

class ContactControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class ContactControllerGetTests {
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
    class ContactControllerPermissionGetTests {
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
        fun `Shouldn't get without token`() {
            transaction {
                GroupEntity.deleteAll()
            }

            mockMvc
                ?.perform(get("/contact"))
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
                ?.perform(get("/contact").header("authorization", token))
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class ContactControllerPostTests {
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
                    val item: ContactResolver =
                        Gson().fromJson(it.response.contentAsString, ContactResolver::class.java)

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
        fun `Shouldn't add without id`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/contact")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"type":"EMAIL"}""")
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
    class ContactControllerPutTests {
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
                }
            }

            mockMvc
                ?.perform(
                    put("/contact?id=SOME")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"Updated", "type": "PHONE"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item: ContactResolver =
                        Gson().fromJson(it.response.contentAsString, ContactResolver::class.java)

                    Assertions.assertEquals("Updated", item.id)
                    Assertions.assertEquals(ContactType.PHONE, item.type)
                }
        }

        @Test
        fun `Shouldn't update without id`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission().also {
                    ContactEntity.insert {
                        it[id] = EntityID("some", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/contact?id=some")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"type":"EMAIL"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't update with wrong id`() {
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
                    put("/contact?id=wrong")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"Updated", "type": "PHONE"}""")
                )
                ?.andExpect(status().isNotFound)
        }

        @Test
        fun `Shouldn't update with blank id`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission().also {
                    ContactEntity.insert {
                        it[id] = EntityID("some", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/contact?id=some")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"", "type": "PHONE"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't update with wrong type`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission().also {
                    ContactEntity.insert {
                        it[id] = EntityID("some", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/contact?id=some")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"updated", "type": "WRONG"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't update without type`() {
            val token = transaction {
                ContactEntity.deleteAll()
                addPermission().also {
                    ContactEntity.insert {
                        it[id] = EntityID("some", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/contact?id=some")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"updated"}""")
                )
                ?.andExpect(status().isBadRequest)
        }

        @Test
        fun `Shouldn't update without method permission`() {
            val token = transaction {
                ContactEntity.deleteAll()

                ContactEntity.insert {
                    it[id] = EntityID("SOME", ContactEntity)
                    it[type] = ContactType.EMAIL
                }
            }

            mockMvc
                ?.perform(
                    put("/contact?id=SOME")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"SOME", "type": "PHONE"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't update without token`() {
            transaction {
                ContactEntity.deleteAll()

                ContactEntity.insert {
                    it[id] = EntityID("SOME", ContactEntity)
                    it[type] = ContactType.EMAIL
                }
            }

            mockMvc
                ?.perform(
                    put("/contact?id=SOME")
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

        @Test
        fun `Shouldn't delete with wrong id`() {
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
                    delete("/contact?id=wrong")
                        .header("authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"SOME", "type": "PHONE"}""")
                )
                ?.andExpect(status().isNotFound)
        }

        @Test
        fun `Shouldn't delete without token`() {
            transaction {
                ContactEntity.deleteAll()

                ContactEntity.insert {
                    it[id] = EntityID("SOME", ContactEntity)
                    it[type] = ContactType.EMAIL
                }
            }

            mockMvc
                ?.perform(
                    delete("/contact?id=wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":"SOME", "type": "PHONE"}""")
                )
                ?.andExpect(status().isForbidden)
        }
    }
}