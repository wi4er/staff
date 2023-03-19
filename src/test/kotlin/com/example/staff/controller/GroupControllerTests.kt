package com.example.staff.controller

import com.example.staff.model.GroupEntity
import com.google.gson.Gson
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
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
@AutoConfigureMockMvc
class GroupControllerTests {
    @Autowired
    private val mockMvc: MockMvc? = null

    @Test
    fun `Should get empty list`() {
        transaction { GroupEntity.deleteAll() }

        mockMvc?.perform(get("/group"))
            ?.andExpect(status().isOk)
            ?.andExpect {

            }
    }

    @Test
    fun `Should post item`() {
        transaction {
            GroupEntity.deleteAll()
        }

        mockMvc?.perform(
            post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"id":1}""")
        )
            ?.andExpect(status().isOk)
            ?.andExpect {
                val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)

                Assertions.assertEquals(1, item.id)
            }
    }

    @Test
    fun `Shouldn't post with duplicate id`() {
        transaction {
            GroupEntity.deleteAll()

            GroupEntity.insert {
                it[GroupEntity.id] = EntityID(1, GroupEntity)
            }
        }

        assertThrows<Exception> {
            mockMvc?.perform(
                post("/group")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"id":1}""")
            )
        }
    }

    @Test
    fun `Should post without id`() {
        transaction {
            GroupEntity.deleteAll()
        }

        mockMvc?.perform(
            post("/group")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{}""")
        )
            ?.andExpect(status().isOk)
            ?.andExpect {
                val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)
                Assertions.assertNotNull(item.id)
            }
    }

    @Test
    fun `Should delete item`() {
        val id: Int = transaction {
            GroupEntity.deleteAll()
            GroupEntity.insertAndGetId {  }.value
        }

        mockMvc?.perform(delete("/group?id=${id}"))
            ?.andExpect(status().isOk)
            ?.andExpect {
                val item = Gson().fromJson(it.response.contentAsString, GroupResolver::class.java)

                Assertions.assertEquals(id, item.id)
            }
    }

    @Test
    fun `Shouldn't delete with wrong id`() {
        transaction { GroupEntity.deleteAll() }

        assertThrows<Exception> {
            mockMvc?.perform(delete("/group?id=77"))
        }
    }
}