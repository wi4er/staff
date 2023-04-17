package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.PropertyInput
import com.example.staff.input.ProviderInput
import com.example.staff.model.*
import com.example.staff.permission.*
import com.example.staff.resolver.PropertyResolver
import com.example.staff.resolver.ProviderResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/property")
class PropertyController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<PropertyResolver> = map {
        PropertyResolver(
            id = it[PropertyEntity.id].value
        )
    }

    @GetMapping
    @CrossOrigin
    fun getList(
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
        response: HttpServletResponse,
    ): List<PropertyResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PROPERTY,
            method = MethodType.GET,
            group = account.groups,
        )

        val count: Int = PropertyEntity
            .selectAll()
            .count()

        response.addIntHeader("Content-Size", count)
        response.addHeader("Access-Control-Expose-Headers", "Content-Size")

        PropertyEntity
            .slice(PropertyEntity.id)
            .selectAll()
            .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: ProviderInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PropertyResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PROPERTY,
            method = MethodType.POST,
            group = account.groups,
        )

        val id: EntityID<String> = try {
            PropertyEntity.insertAndGetId {
                it[id] = EntityID(input.id ?: throw StaffException("Property id expected"), ProviderEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong provider")
        }

        PropertyEntity
            .select { PropertyEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw PermissionException("Permission denied!")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: PropertyInput,
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PropertyResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PROPERTY,
            method = MethodType.PUT,
            group = account.groups,
        )

        try {
            PropertyEntity.update(
                where = { PropertyEntity.id eq id }
            ) {
                it[PropertyEntity.id] = EntityID(input.id, PropertyEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong property")
        }

        PropertyEntity
            .select { PropertyEntity.id eq input.id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("Property not found")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): PropertyResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PROPERTY,
            method = MethodType.DELETE,
            group = account.groups,
        )

        PropertyEntity
            .select { PropertyEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?.also { PropertyEntity.deleteWhere { PropertyEntity.id eq id } }
            ?: throw NoDataException("Wrong property")
    }
}