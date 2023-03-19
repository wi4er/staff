package com.example.staff.controller

import com.example.staff.model.UserEntity
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
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTests {

    @Autowired
    private val mockMvc: MockMvc? = null

    @Test
    fun `Should get empty list`() {
        transaction {
            UserEntity.deleteAll()
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
    fun `Should post item`() {
        transaction {
            UserEntity.deleteAll()
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
    fun `Should put item`() {
        transaction {
            UserEntity.deleteAll()

            UserEntity.insert {
                it[id] = EntityID(1, table = UserEntity)
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
    fun `Shouldn't put item with wrong id`() {
        transaction {
            UserEntity.deleteAll()
        }

        Assertions.assertThrows(Exception::class.java) {
            mockMvc
                ?.perform(
                    put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"id":1, "login":"NEW_NAME"}""")
                )
        }
    }

    @Test
    fun `Should delete item`() {
        transaction {
            UserEntity.deleteAll()

            UserEntity.insert {
                it[id] = EntityID(1, table = UserEntity)
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
    fun `Shouldn't delete item with wrong id`() {
        transaction {
            UserEntity.deleteAll()
        }

        Assertions.assertThrows(Exception::class.java) {
            mockMvc?.perform(delete("/user?id=1"))
        }
    }
}