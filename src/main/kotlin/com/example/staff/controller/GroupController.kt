package com.example.staff.controller

import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.GroupInput
import com.example.staff.model.GroupEntity
import com.example.staff.permission.*
import com.example.staff.resolver.GroupResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/group")
class GroupController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<GroupResolver> {
        return map {
            GroupResolver(
                id = it[GroupEntity.id].value,
                parent = it[GroupEntity.parent]?.value,
            )
        }
    }

    @GetMapping
    @CrossOrigin
    fun getList(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): List<GroupResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.GROUP,
            method = MethodType.GET,
            group = account.groups,
        )

        GroupEntity
            .selectAll()
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: GroupInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): GroupResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.GROUP,
            method = MethodType.POST,
            group = account.groups,
        )

        val id: EntityID<Int> = try {
            GroupEntity.insertAndGetId { entity ->
                input.id?.let { entity[GroupEntity.id] = EntityID(input.id, GroupEntity) }
                input.parent?.let {
                    entity[parent] = EntityID(it, GroupEntity)
                }
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong group")
        }

        GroupEntity
            .select { GroupEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw Exception("Wrong group")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: GroupInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): GroupResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.GROUP,
            method = MethodType.PUT,
            group = account.groups,
        )

        try {
            GroupEntity.update(
                where = { GroupEntity.id eq input.id }
            ) { entity ->
                input.parent
                    ?.let { entity[parent] = EntityID(it, GroupEntity) }
                    ?: run { entity[parent] = null }
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong group")
        }

        GroupEntity
            .select { GroupEntity.id eq input.id }
            .toResolver()
            .firstOrNull()
            ?: throw Exception("User group not found")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: Int,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): GroupResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.GROUP,
            method = MethodType.DELETE,
            group = account.groups,
        )

        GroupEntity.select { GroupEntity.id eq id }
            .toResolver()
            .firstOrNull()
            .also { GroupEntity.deleteWhere { GroupEntity.id eq id } }
            ?: throw Exception("Wrong group")
    }
}