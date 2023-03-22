package com.example.staff.controller

import com.example.staff.exception.PermissionException
import com.example.staff.model.*
import com.example.staff.resolver.UserResolver
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {
    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerGetTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        fun addPermission() {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }
        }

        @Test
        fun `Should get empty list`() {
            transaction {
                UserEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(get("/user"))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)
                    Assertions.assertEquals(0, list.size)
                }
        }

        @Test
        fun `Shouldn't get without permission`() {
            transaction {
                UserEntity.deleteAll()
                MethodPermissionEntity.deleteAll()
            }

            assertThrows<Exception> {
                mockMvc?.perform(get("/user"))
            }
        }

        @Test
        fun `Should get user with group`() {
            transaction {
                UserEntity.deleteAll()
                GroupEntity.deleteAll()

                addPermission()

                val userId = UserEntity.insertAndGetId { it[login] = "user_name" }
                val groupId = GroupEntity.insertAndGetId { it[id] = EntityID(22, GroupEntity) }

                User2GroupEntity.insert {
                    it[user] = userId
                    it[group] = groupId
                }
            }

            mockMvc
                ?.perform(get("/user"))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)

                    Assertions.assertEquals(1, list.size)
                    Assertions.assertEquals(1, list.first().group.size)
                    Assertions.assertEquals(22, list.first().group.first())
                }
        }

        @Test
        fun `Should get user list with groups`() {
            transaction {
                UserEntity.deleteAll()
                GroupEntity.deleteAll()

                addPermission()

                val group1 = GroupEntity.insertAndGetId { it[id] = EntityID(1, GroupEntity) }
                val group2 = GroupEntity.insertAndGetId { it[id] = EntityID(2, GroupEntity) }

                for (i in 1..100) {
                    val userId = UserEntity.insertAndGetId { it[login] = "user_name_${i}" }

                    User2GroupEntity.insert {
                        it[user] = userId
                        it[group] = group1
                    }
                    User2GroupEntity.insert {
                        it[user] = userId
                        it[group] = group2
                    }
                }
            }

            mockMvc
                ?.perform(get("/user"))
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val list = Gson().fromJson(it.response.contentAsString, Array<UserResolver>::class.java)

                    Assertions.assertEquals(100, list.size)
                    Assertions.assertEquals(listOf(1, 2), list.first().group)
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerPostTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        fun addPermission() {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }
        }

        @Test
        fun `Should post item`() {
            transaction {
                UserEntity.deleteAll()
                addPermission()
            }

            mockMvc
                ?.perform(
                    post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"login":"root_admin"}""")
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
                GroupEntity.deleteAll()
            }

            assertThrows<Exception> {
                mockMvc?.perform(
                    post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"login":"root_admin"}""")
                )
            }
        }

        @Test
        fun `Should post with group`() {
            transaction {
                UserEntity.deleteAll()
                addPermission()

                GroupEntity.insert { it[GroupEntity.id] = EntityID(33, GroupEntity) }
            }

            mockMvc
                ?.perform(
                    post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"login":"root_admin", "group": [33]}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals(1, item.group.size)
                    Assertions.assertEquals(33, item.group.first())
                }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerPutTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        fun addPermission() {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }
        }

        @Test
        fun `Should put user`() {
            transaction {
                UserEntity.deleteAll()
                addPermission()

                UserEntity.insert {
                    it[id] = EntityID(1, UserEntity)
                    it[login] = "OLD_NAME"
                }
            }

            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME"}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals("NEW_NAME", item.login)
                }
        }

        @Test
        fun `Should add group to user`() {
            transaction {
                UserEntity.deleteAll()
                addPermission()

                UserEntity.insert {
                    it[id] = EntityID(1, UserEntity)
                    it[login] = "OLD_NAME"
                }
                GroupEntity.insert { it[id] = EntityID(33, UserEntity) }
            }

            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME", "group": [33]}""")
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
            transaction {
                UserEntity.deleteAll()
                addPermission()

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

            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME", "group": []}""")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals(0, item.group.size)
                }
        }

        @Test
        fun `Should change group from user`() {
            transaction {
                UserEntity.deleteAll()
                addPermission()

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

            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME", "group": [44]}""")
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
            transaction {
                UserEntity.deleteAll()
                addPermission()
            }

            Assertions.assertThrows(Exception::class.java) {
                mockMvc?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME"}""")
                )
            }
        }
    }

    @SpringBootTest
    @AutoConfigureMockMvc
    class UserControllerDeleteTest {
        @Autowired
        private val mockMvc: MockMvc? = null

        fun addPermission() {
            GroupEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }
        }

        @Test
        fun `Should delete item`() {
            transaction {
                UserEntity.deleteAll()
                addPermission()

                UserEntity.insert {
                    it[id] = EntityID(1, UserEntity)
                    it[login] = "OLD_NAME"
                }
            }

            mockMvc
                ?.perform(
                    delete("/user?id=1")
                )
                ?.andExpect(status().isOk)
                ?.andExpect {
                    val item = Gson().fromJson(it.response.contentAsString, UserResolver::class.java)
                    Assertions.assertEquals("OLD_NAME", item.login)
                }
        }

        @Test
        fun `Shouldn't delete without permission`() {
            transaction {
                UserEntity.deleteAll()
                GroupEntity.deleteAll()
            }

            assertThrows<Exception> {
                mockMvc?.perform(delete("/user?id=1"))
            }
        }

        @Test
        fun `Shouldn't delete item with wrong id`() {
            transaction {
                UserEntity.deleteAll()
                addPermission()
            }

            Assertions.assertThrows(Exception::class.java) {
                mockMvc?.perform(delete("/user?id=1"))
            }
        }
    }
}