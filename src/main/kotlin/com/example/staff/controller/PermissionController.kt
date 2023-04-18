package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.PermissionInput
import com.example.staff.model.GroupEntity
import com.example.staff.model.MethodPermissionEntity
import com.example.staff.model.UserEntity
import com.example.staff.model.UserPermissionEntity
import com.example.staff.permission.*
import com.example.staff.resolver.PermissionResolver
import com.example.staff.resolver.UserResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/permission")
class PermissionController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<PermissionResolver> {
        return map {
            PermissionResolver(
                method = it[MethodPermissionEntity.method],
                entity = it[MethodPermissionEntity.entity],
                group = it[MethodPermissionEntity.group].value,
            )
        }
    }

    @GetMapping
    @CrossOrigin
    fun getList(
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
        response: HttpServletResponse,
    ): List<PermissionResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PERMISSION,
            method = MethodType.GET,
            group = account.groups,
        )

        val count: Int = MethodPermissionEntity
            .selectAll()
            .count()

        response.addIntHeader("Content-Size", count)
        response.addHeader("Access-Control-Expose-Headers", "Content-Size")

        MethodPermissionEntity
            .selectAll()
            .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: PermissionInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PermissionResolver = transaction {
        authorization  ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PERMISSION,
            method = MethodType.POST,
            group = account.groups,
        )

        MethodPermissionEntity.insertAndGetId {
            it[method] = input.method ?: throw StaffException("Wrong permission")
            it[entity] = input.entity ?: throw StaffException("Wrong permission")
            it[group] = EntityID(input.group, GroupEntity)
        }.let { id ->
            MethodPermissionEntity
                .select { MethodPermissionEntity.id eq id }
                .toResolver()
                .firstOrNull()
        } ?: throw Exception("Wrong permission")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: PermissionInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PermissionResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PERMISSION,
            method = MethodType.PUT,
            group = account.groups,
        )

        MethodPermissionEntity.update(
            where = { MethodPermissionEntity.id eq input.id }
        ) {
            it[entity] = input.entity ?: throw StaffException("Wrong permission")
            it[method] = input.method ?: throw StaffException("Wrong permission")
            it[group] = EntityID(input.group, GroupEntity)
        }.let {
            MethodPermissionEntity
                .select { MethodPermissionEntity.id eq it }
                .toResolver()
                .firstOrNull()
        } ?: throw NoDataException("Wrong permission")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: Int,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PermissionResolver  = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PERMISSION,
            method = MethodType.DELETE,
            group = account.groups,
        )

        MethodPermissionEntity
            .select { MethodPermissionEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?.also { MethodPermissionEntity.deleteWhere { MethodPermissionEntity.id eq id } }
            ?: throw NoDataException("Wrong permission")
    }
}