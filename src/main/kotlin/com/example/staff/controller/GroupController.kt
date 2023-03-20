package com.example.staff.controller

import com.example.staff.input.GroupInput
import com.example.staff.model.GroupEntity
import com.example.staff.resolver.GroupResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/group")
class GroupController {
    fun Query.toResolver(): List<GroupResolver> {
        return map {
            GroupResolver(
                id = it[GroupEntity.id].value,
                parent = it[GroupEntity.parent]?.value,
            )
        }
    }

    @GetMapping
    fun getList(): List<GroupResolver> = transaction {
        GroupEntity.selectAll().toResolver()
    }

    @PostMapping
    fun addItem(
        @RequestBody input: GroupInput
    ): GroupResolver = transaction {
        GroupEntity.insertAndGetId { entity ->
            input.id?.let { entity[GroupEntity.id] = EntityID(input.id, GroupEntity) }
            input.parent?.let {
                entity[parent] = EntityID(it, GroupEntity)
            }
        }.let {
            GroupEntity
                .select { GroupEntity.id eq it }
                .toResolver()
                .firstOrNull()
        } ?: throw Exception("Wrong group")
    }

    @PutMapping
    fun updateItem(
        @RequestBody input: GroupInput
    ): GroupResolver = transaction {
        GroupEntity.update(
            where = { GroupEntity.id eq input.id }
        ) { entity ->
            input.parent?.let {
                entity[parent] = EntityID(it, GroupEntity)
            } ?: run {
                entity[parent] = null
            }
        }.let {
            GroupEntity
                .select { GroupEntity.id eq input.id }
                .toResolver()
                .firstOrNull()
        } ?: throw Exception("User group not found")
    }

    @DeleteMapping
    fun deleteItem(
        @RequestParam id: Int
    ): GroupResolver = transaction {
        GroupEntity.select { GroupEntity.id eq id }
            .toResolver()
            .firstOrNull()
            .also { GroupEntity.deleteWhere { GroupEntity.id eq id } }
            ?: throw Exception("Wrong group")
    }
}