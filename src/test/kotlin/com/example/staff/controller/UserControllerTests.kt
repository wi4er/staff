package com.example.staff.controller

import com.example.staff.model.*
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import com.example.staff.permission.UserAccount
import com.example.staff.resolver.UserResolver
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerGetTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.USER
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
                ?.perform(get("/user").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)
                    Assertions.assertEquals(0, list.size)
                }
        }

        @Test
        fun `Should get full list`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    for (i in 1..10) {
                        UserEntity.insert {
                            it[id] = EntityID(i, UserEntity)
                            it[login] = "user_${i}"
                        }

                        UserPermissionEntity.insert {
                            it[user] = EntityID(i, UserEntity)
                            it[method] = MethodType.GET
                            it[group] = EntityID(777, GroupEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/user").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)
                    Assertions.assertEquals(10, list.size)
                }
        }

        @Test
        fun `Should get list count`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    for (i in 1..10) {
                        UserEntity.insert {
                            it[id] = EntityID(i, UserEntity)
                            it[login] = "user_${i}"
                        }

                        UserPermissionEntity.insert {
                            it[user] = EntityID(i, UserEntity)
                            it[method] = MethodType.GET
                            it[group] = EntityID(777, GroupEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/user").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect(header().string("Content-Size", "10"))
        }

        @Test
        fun `Should get list with limit`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    for (i in 1..10) {
                        UserEntity.insert {
                            it[id] = EntityID(i, UserEntity)
                            it[login] = "user_${i}"
                        }

                        UserPermissionEntity.insert {
                            it[user] = EntityID(i, UserEntity)
                            it[method] = MethodType.GET
                            it[group] = EntityID(777, GroupEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/user?limit=4").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)
                    Assertions.assertEquals(4, list.size)
                }
        }

        @Test
        fun `Should get list with offset`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    for (i in 1..10) {
                        UserEntity.insert {
                            it[id] = EntityID(i, UserEntity)
                            it[login] = "user_${i}"
                        }

                        UserPermissionEntity.insert {
                            it[user] = EntityID(i, UserEntity)
                            it[method] = MethodType.GET
                            it[group] = EntityID(777, GroupEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/user?offset=4").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)
                    Assertions.assertEquals(6, list.size)
                }
        }

        @Test
        fun `Should get user with group`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    val userId = UserEntity.insertAndGetId {
                        it[id] = EntityID(333, UserEntity)
                        it[login] = "user_name"
                    }
                    val groupId = GroupEntity.insertAndGetId { it[id] = EntityID(22, GroupEntity) }

                    User2GroupEntity.insert {
                        it[user] = userId
                        it[group] = groupId
                    }

                    UserPermissionEntity.insert {
                        it[user] = EntityID(333, UserEntity)
                        it[method] = MethodType.GET
                        it[group] = EntityID(777, GroupEntity)
                    }
                }
            }

            mockMvc
                ?.perform(get("/user").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)

                    Assertions.assertEquals(1, list.size)
                    Assertions.assertEquals(1, list.first().group.size)
                    Assertions.assertEquals(listOf(22), list.first().group)
                }
        }

        @Test
        fun `Should get user with contact`() {
            val token = transaction {
                UserEntity.deleteAll()
                ContactEntity.deleteAll()

                addPermission().also {
                    val userId = UserEntity.insertAndGetId {
                        it[id] = EntityID(333, UserEntity)
                        it[login] = "user_name"
                    }
                    val contactId = ContactEntity.insertAndGetId {
                        it[id] = EntityID("mail", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }

                    User2ContactEntity.insert {
                        it[user] = userId
                        it[contact] = contactId
                        it[value] = "my_mail@mail.com"
                    }

                    UserPermissionEntity.insert {
                        it[user] = EntityID(333, UserEntity)
                        it[method] = MethodType.GET
                        it[group] = EntityID(777, GroupEntity)
                    }
                }
            }

            mockMvc
                ?.perform(get("/user").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)

                    Assertions.assertEquals(1, list.size)
                    Assertions.assertEquals(1, list.first().contact.size)
                    Assertions.assertEquals("mail", list.first().contact.firstOrNull()?.contact)
                    Assertions.assertEquals("my_mail@mail.com", list.first().contact.firstOrNull()?.value)
                }
        }

        @Test
        fun `Should get user with group filter`() {
//            val token = transaction {
//                UserEntity.deleteAll()
//
//                addPermission().also {
//                    val userId = UserEntity.insertAndGetId {
//                        it[id] = EntityID(333, UserEntity)
//                        it[login] = "user_name"
//                    }
//                    val groupId = GroupEntity.insertAndGetId { it[id] = EntityID(22, GroupEntity) }
//
//                    UserEntity.insert { it[login] = "another_name" }
//
//                    User2GroupEntity.insert {
//                        it[user] = userId
//                        it[group] = groupId
//                    }
//                    UserPermissionEntity.insert {
//                        it[user] = EntityID(333, UserEntity)
//                        it[method] = MethodType.GET
//                        it[group] = EntityID(777, GroupEntity)
//                    }
//                }
//            }
//
//            mockMvc
//                ?.perform(get("/user?filter[group]=22").header("authorization", token))
//                ?.andExpect(status().isOk)
//                ?.andExpect {
//                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)
//
//                    Assertions.assertEquals(1, list.size)
//                    Assertions.assertEquals("user_name", list.first().login)
//                }
        }

        @Test
        fun `Should get user list with groups`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    val group1 = GroupEntity.insertAndGetId { it[id] = EntityID(1, GroupEntity) }
                    val group2 = GroupEntity.insertAndGetId { it[id] = EntityID(2, GroupEntity) }

                    for (i in 1..100) {
                        val userId = UserEntity.insertAndGetId {
                            it[id] = EntityID(i, UserEntity)
                            it[login] = "user_name_${i.toString().padStart(3, '0')}"
                        }

                        User2GroupEntity.insert {
                            it[user] = userId
                            it[group] = group1
                        }
                        User2GroupEntity.insert {
                            it[user] = userId
                            it[group] = group2
                        }
                        UserPermissionEntity.insert {
                            it[user] = userId
                            it[method] = MethodType.GET
                            it[group] = EntityID(777, GroupEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/user").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)

                    Assertions.assertEquals(100, list.size)
                    Assertions.assertEquals(listOf(1, 2), list.first().group)
                }
        }

        @Test
        fun `Should get user list with groups and limit`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    val group1 = GroupEntity.insertAndGetId { it[id] = EntityID(1, GroupEntity) }
                    val group2 = GroupEntity.insertAndGetId { it[id] = EntityID(2, GroupEntity) }

                    for (i in 1..20) {
                        val userId = UserEntity.insertAndGetId {
                            it[id] = EntityID(i, UserEntity)
                            it[login] = "user_name_${i.toString().padStart(3, '0')}"
                        }

                        User2GroupEntity.insert {
                            it[user] = userId
                            it[group] = group1
                        }
                        User2GroupEntity.insert {
                            it[user] = userId
                            it[group] = group2
                        }
                        UserPermissionEntity.insert {
                            it[user] = userId
                            it[method] = MethodType.GET
                            it[group] = EntityID(777, GroupEntity)
                        }
                    }
                }
            }

            mockMvc
                ?.perform(get("/user?limit=10").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)

                    Assertions.assertEquals(10, list.size)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerPermissionGetTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should get list without permission`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    for (i in 1..10) {
                        UserEntity.insert {
                            it[id] = EntityID(i, UserEntity)
                            it[login] = "user_${i}"
                        }
                    }
                }
            }

            println(token)

            mockMvc
                ?.perform(get("/user").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)
                    Assertions.assertEquals(0, list.size)
                }
        }

        @Test
        fun `Should get list with partial permission`() {
            transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    GroupEntity.insert { it[id] = EntityID(888, GroupEntity) }

                    for (i in 1..10) {
                        UserEntity.insert {
                            it[id] = EntityID(i, UserEntity)
                            it[login] = "user_${i}"
                        }

                        UserPermissionEntity.insert {
                            it[user] = EntityID(i, UserEntity)
                            it[method] = MethodType.GET
                            it[group] = EntityID(888, GroupEntity)
                        }

                        UserPermissionEntity.insert {
                            it[user] = EntityID(i, UserEntity)
                            it[method] = MethodType.GET
                            it[group] = EntityID(777, GroupEntity)
                        }
                    }
                }
            }

            val token = accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777, 888))) ?: ""

            mockMvc
                ?.perform(get("/user").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)
                    Assertions.assertEquals(10, list.size)
                }
        }

        @Test
        fun `Shouldn't get without method permission`() {
            transaction {
                UserEntity.deleteAll()
                MethodPermissionEntity.deleteAll()
            }

            mockMvc
                ?.perform(get("/user"))
                ?.andExpect(status().isForbidden)
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerPostTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should post item`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"login":"root_admin"}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals("root_admin", item.login)
                }
        }

        @Test
        fun `Shouldn't post without permission`() {
            transaction {
                UserEntity.deleteAll()
            }

            mockMvc
                ?.perform(
                    post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"login":"root_admin"}""")
                )
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Should post with group`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission().also {
                    GroupEntity.insert { it[GroupEntity.id] = EntityID(33, GroupEntity) }
                }
            }

            mockMvc
                ?.perform(
                    post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"login":"root_admin", "group": [33]}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals(1, item.group.size)
                    Assertions.assertEquals(33, item.group.first())
                }
        }

        @Test
        fun `Should post with contact`() {
            val token = transaction {
                UserEntity.deleteAll()
                ContactEntity.deleteAll()

                addPermission().also {
                    ContactEntity.insert {
                        it[id] = EntityID("mail", ContactEntity)
                        it[type] = ContactType.EMAIL
                    }
                }
            }

            mockMvc
                ?.perform(
                    post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"login":"root_admin", "contact": [{"contact":  "mail", "value": "mail@mail.com"}]}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals(1, item.contact.size)
                    Assertions.assertEquals("mail@mail.com", item.contact.first().value)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerPutTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        @Autowired
        private val accountFactory: AccountFactory? = null

        fun addPermission(): String {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should put user`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission().also {
                    UserEntity.insert {
                        it[id] = EntityID(1, UserEntity)
                        it[login] = "OLD_NAME"
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME"}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals("NEW_NAME", item.login)
                }
        }

        @Test
        fun `Should add group to user`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission().also {
                    UserEntity.insert {
                        it[id] = EntityID(1, UserEntity)
                        it[login] = "OLD_NAME"
                    }
                    GroupEntity.insert { it[id] = EntityID(33, UserEntity) }
                }
            }

            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME", "group": [33]}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals(1, item.group.size)
                    Assertions.assertEquals(33, item.group.first())
                }
        }

        @Test
        fun `Should remove group from user`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission().also {
                    UserEntity.insert {
                        it[id] = EntityID(1, UserEntity)
                        it[login] = "OLD_NAME"
                    }
                    GroupEntity.insert { it[id] = EntityID(33, UserEntity) }
                    User2GroupEntity.insert {
                        it[group] = EntityID(33, GroupEntity)
                        it[user] = EntityID(1, UserEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME", "group": []}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals(0, item.group.size)
                }
        }

        @Test
        fun `Should change group from user`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission().also {
                    UserEntity.insert {
                        it[id] = EntityID(1, UserEntity)
                        it[login] = "OLD_NAME"
                    }
                    GroupEntity.insert { it[id] = EntityID(33, UserEntity) }
                    GroupEntity.insert { it[id] = EntityID(44, UserEntity) }
                    User2GroupEntity.insert {
                        it[group] = EntityID(33, GroupEntity)
                        it[user] = EntityID(1, UserEntity)
                    }
                }
            }

            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME", "group": [44]}""")
                        .header("authorization", token)
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals(1, item.group.size)
                    Assertions.assertEquals(44, item.group.first())
                }
        }

        @Test
        fun `Shouldn't put item with wrong id`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission()
            }

            assertThrows<Exception> {
                mockMvc?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME"}""")
                        .header("authorization", token)
                )
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
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }

            return accountFactory?.createToken(UserAccount(id = 1, groups = listOf(777))) ?: ""
        }

        @Test
        fun `Should delete item`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    UserEntity.insert {
                        it[id] = EntityID(1, UserEntity)
                        it[login] = "OLD_NAME"
                    }

                    UserPermissionEntity.insert {
                        it[user] = EntityID(1, UserEntity)
                        it[method] = MethodType.DELETE
                        it[group] = EntityID(777, UserEntity)
                    }
                }
            }

            mockMvc
                ?.perform(delete("/user?id=1").header("authorization", token))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals("OLD_NAME", item.login)
                }
        }

        @Test
        fun `Shouldn't delete without item permission`() {
            val token = transaction {
                UserEntity.deleteAll()

                addPermission().also {
                    UserEntity.insert {
                        it[id] = EntityID(1, UserEntity)
                        it[login] = "OLD_NAME"
                    }
                }
            }

            mockMvc
                ?.perform(delete("/user?id=1").header("authorization", token))
                ?.andExpect(status().isNotFound)
        }

        @Test
        fun `Shouldn't delete without method permission`() {
            val token = transaction {
                UserEntity.deleteAll()

                UserEntity.insert {
                    it[id] = EntityID(1, UserEntity)
                    it[login] = "OLD_NAME"
                }
            }

            mockMvc
                ?.perform(delete("/user?id=1").header("authorization", token))
                ?.andExpect(status().isForbidden)
        }

        @Test
        fun `Shouldn't delete item with wrong id`() {
            val token = transaction {
                UserEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(delete("/user?id=1").header("authorization", token))
                ?.andExpect(status().isNotFound)
        }
    }
}