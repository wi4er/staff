package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.ContactInput
import com.example.staff.input.ProviderInput
import com.example.staff.model.ContactEntity
import com.example.staff.model.ContactPermissionEntity
import com.example.staff.model.ContactType
import com.example.staff.model.ProviderEntity
import com.example.staff.permission.*
import com.example.staff.resolver.ContactResolver
import com.example.staff.resolver.ProviderResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/provider")
class ProviderController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {

    fun Query.toResolver(): List<ProviderResolver> {
        return map {
            ProviderResolver(
                id = it[ProviderEntity.id].value
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
    ): List<ProviderResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PROVIDER,
            method = MethodType.GET,
            group = account.groups,
        )

        val count: Int = ProviderEntity
            .selectAll()
            .count()

        response.addIntHeader("Content-Size", count)
        response.addHeader("Access-Control-Expose-Headers", "Content-Size")

        ProviderEntity
            .slice(ProviderEntity.id)
            .selectAll()
            .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: ProviderInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): ProviderResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PROVIDER,
            method = MethodType.POST,
            group = account.groups,
        )

        val id: EntityID<String> = try {
            ProviderEntity.insertAndGetId {
                it[id] = EntityID(input.id, ProviderEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong provider")
        }

        ProviderEntity
            .select { ProviderEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw PermissionException("Permission denied!")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: ProviderInput,
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): ProviderResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PROVIDER,
            method = MethodType.PUT,
            group = account.groups,
        )

        try {
            ProviderEntity.update(
                where = { ProviderEntity.id eq id }
            ) {
                it[ProviderEntity.id] = EntityID(input.id, ProviderEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong provider")
        }

        ProviderEntity
            .select { ProviderEntity.id eq input.id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("Wrong provider")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): ProviderResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.PROVIDER,
            method = MethodType.DELETE,
            group = account.groups,
        )

        ProviderEntity
            .select { ProviderEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?.also { ProviderEntity.deleteWhere { ProviderEntity.id eq id } }
            ?: throw NoDataException("Wrong provider")
    }
}