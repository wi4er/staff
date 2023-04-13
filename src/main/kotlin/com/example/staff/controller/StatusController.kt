package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.ProviderInput
import com.example.staff.input.StatusInput
import com.example.staff.model.*
import com.example.staff.permission.*
import com.example.staff.resolver.StatusResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/status")
class StatusController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<StatusResolver> = map {
        StatusResolver(
            id = it[StatusEntity.id].value
        )
    }

    @GetMapping
    @CrossOrigin
    fun getList(
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
        response: HttpServletResponse,
    ): List<StatusResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.STATUS,
            method = MethodType.GET,
            group = account.groups,
        )

        val count: Int = StatusEntity
            .selectAll()
            .count()

        response.addIntHeader("Content-Size", count)
        response.addHeader("Access-Control-Expose-Headers", "Content-Size")

        StatusEntity
            .slice(StatusEntity.id)
            .selectAll()
            .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: StatusInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): StatusResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.STATUS,
            method = MethodType.POST,
            group = account.groups,
        )

        val id: EntityID<String> = try {
            StatusEntity.insertAndGetId {
                it[id] = EntityID(input.id ?: throw StaffException("Status id expected"), StatusEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong status")
        }

        StatusEntity
            .select { StatusEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw PermissionException("Permission denied!")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: StatusInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): StatusResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.STATUS,
            method = MethodType.PUT,
            group = account.groups,
        )

        try {
            StatusEntity.update(
                where = { StatusEntity.id eq input.id }
            ) {
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong status")
        }

        StatusEntity
            .select { StatusEntity.id eq input.id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("Wrong status")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): StatusResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.STATUS,
            method = MethodType.DELETE,
            group = account.groups,
        )

        StatusEntity
            .select { StatusEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?.also { StatusEntity.deleteWhere { StatusEntity.id eq id } }
            ?: throw NoDataException("Wrong status")
    }
}