package com.example.staff.controller

import com.example.staff.exception.PermissionException
import com.example.staff.input.GroupInput
import com.example.staff.model.EntityType
import com.example.staff.model.GroupEntity
import com.example.staff.model.MethodType
import com.example.staff.permission.Account
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.MethodPermissionService
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
    fun getList(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): List<GroupResolver> = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.GROUP,
                method = MethodType.GET,
                group = account.groups,
            )

            GroupEntity.selectAll().toResolver()
        } ?: throw PermissionException("Permission denied!")
    }

    @PostMapping
    fun addItem(
        @RequestBody input: GroupInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): GroupResolver = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.GROUP,
                method = MethodType.POST,
                group = account.groups,
            )

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
        } ?: throw PermissionException("Permission denied!")
    }

    @PutMapping
    fun updateItem(
        @RequestBody input: GroupInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): GroupResolver = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.GROUP,
                method = MethodType.PUT,
                group = account.groups,
            )

            GroupEntity.update(
                where = { GroupEntity.id eq input.id }
            ) { entity ->
                input.parent
                    ?.let { entity[parent] = EntityID(it, GroupEntity) }
                    ?: run { entity[parent] = null }
            }.let {
                GroupEntity
                    .select { GroupEntity.id eq input.id }
                    .toResolver()
                    .firstOrNull()
            } ?: throw Exception("User group not found")
        } ?: throw PermissionException("Permission denied!")
    }

    @DeleteMapping
    fun deleteItem(
        @RequestParam id: Int,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): GroupResolver = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
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
        } ?: throw PermissionException("Permission denied!")
    }
}