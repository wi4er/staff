package com.example.staff

import com.example.staff.model.*
import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@SpringBootTest
@AutoConfigureMockMvc
class StaffApplicationTests {
    @Autowired
    private val mockMvc: MockMvc? = null

    @Test
    fun contextLoads() {
        mockMvc
            ?.perform(get("/"))
            ?.andExpect {
                println(it.response.contentAsString)
            }
    }

    @Test
    fun `Should populate`() {
        transaction {
            UserEntity.deleteAll()
            GroupEntity.deleteAll()
            LangEntity.deleteAll()
            MethodPermissionEntity.deleteAll()

            GroupEntity.insert { it[id] = EntityID(777, GroupEntity) }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.USER
                it[group] = EntityID(777, GroupEntity)
            }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.PROPERTY
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.PROPERTY
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.PROPERTY
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.PROPERTY
                it[group] = EntityID(777, GroupEntity)
            }

            MethodPermissionEntity.insert {
                it[method] = MethodType.GET
                it[entity] = EntityType.LANG
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.POST
                it[entity] = EntityType.LANG
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.PUT
                it[entity] = EntityType.LANG
                it[group] = EntityID(777, GroupEntity)
            }
            MethodPermissionEntity.insert {
                it[method] = MethodType.DELETE
                it[entity] = EntityType.LANG
                it[group] = EntityID(777, GroupEntity)
            }

            val group1 = GroupEntity.insertAndGetId { it[id] = EntityID(1, GroupEntity) }
            val group2 = GroupEntity.insertAndGetId { it[id] = EntityID(2, GroupEntity) }
            val lang1 = LangEntity.insertAndGetId { it[id] = EntityID("EN", LangEntity) }
            val lang2 = LangEntity.insertAndGetId { it[id] = EntityID("GR", LangEntity) }

            for (i in 1..83) {
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
}
