package com.example.staff.controller

import com.example.staff.model.UserEntity
import com.example.staff.model.GroupEntity
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.*


class GroupResolver(item: ResultRow) {
    val id: Int = item[GroupEntity.id].value
}

class GroupInput() {
    var id: Int? = null
}

@RestController
@RequestMapping("/group")
class GroupController {
    @GetMapping
    fun getList(): List<GroupResolver> = transaction {
        GroupEntity.selectAll().map(::GroupResolver)
    }

    @PostMapping
    fun addItem(
        @RequestBody input: GroupInput
    ): GroupResolver = transaction {
        GroupEntity.insertAndGetId { entity ->
            input.id?.let { entity[GroupEntity.id] = EntityID(input.id, GroupEntity) }
        }.let {
            GroupEntity
                .select { GroupEntity.id eq it }
                .firstOrNull()
                ?.let(::GroupResolver)
        } ?: throw Exception("Wrong group")
    }

    @PutMapping
    fun updateItem(
        @RequestBody input: GroupInput
    ): GroupResolver = transaction {
        GroupEntity.update(
            where = { GroupEntity.id eq input.id }
        ) {

        }.let {
            GroupEntity.select { GroupEntity.id eq input.id }
                .firstOrNull()?.let(::GroupResolver)
        } ?: throw Exception("User group not found")
    }

    @DeleteMapping
    fun deleteItem(
        @RequestParam id: Int
    ): GroupResolver = transaction {
        GroupEntity.select { GroupEntity.id eq id }
            .firstOrNull()
            ?.let(::GroupResolver)
            .also { GroupEntity.deleteWhere { GroupEntity.id eq id } }
            ?: throw Exception("Wrong group")
    }
}