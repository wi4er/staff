package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.PointInput
import com.example.staff.input.PropertyInput
import com.example.staff.input.ProviderInput
import com.example.staff.model.*
import com.example.staff.permission.*
import com.example.staff.resolver.PointResolver
import com.example.staff.resolver.PropertyResolver
import com.example.staff.resolver.ProviderResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/point")
class PointController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<PointResolver> = map {
        PointResolver(
            id = it[PointEntity.id].value,
            directory = it[PointEntity.directory].value,
        )
    }

    @GetMapping
    @CrossOrigin
    fun getList(
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
        response: HttpServletResponse,
    ): List<PointResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.POINT,
            method = MethodType.GET,
            group = account.groups,
        )

        val count: Int = PointEntity
            .selectAll()
            .count()

        response.addIntHeader("Content-Size", count)
        response.addHeader("Access-Control-Expose-Headers", "Content-Size")

        PointEntity
            .slice(PointEntity.id, PointEntity.directory)
            .selectAll()
            .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: PointInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PointResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.POINT,
            method = MethodType.POST,
            group = account.groups,
        )

        val id: EntityID<String> = try {
            PointEntity.insertAndGetId {
                it[id] = EntityID(input.id ?: throw StaffException("Point id expected"), PointEntity)
                it[directory] = EntityID(input.directory ?: throw StaffException("Point directory expected"), PointEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong point")
        }

        PointEntity
            .select { PointEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("Wrong point!")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: PointInput,
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PointResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.POINT,
            method = MethodType.PUT,
            group = account.groups,
        )

        try {
            PointEntity.update(
                where = { PointEntity.id eq id }
            ) {
                it[this.id] = EntityID(input.id, PointEntity)
                it[directory] = EntityID(input.directory, DirectoryEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong point")
        }

        PointEntity
            .select { PointEntity.id eq input.id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("Point not found")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PointResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.POINT,
            method = MethodType.DELETE,
            group = account.groups,
        )

        PointEntity
            .select { PointEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?.also { PointEntity.deleteWhere { PointEntity.id eq id } }
            ?: throw NoDataException("Wrong property")
    }
}