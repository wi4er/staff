package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.PropertyInput
import com.example.staff.input.ProviderInput
import com.example.staff.model.*
import com.example.staff.permission.*
import com.example.staff.resolver.LangResolver
import com.example.staff.resolver.PropertyResolver
import com.example.staff.resolver.ProviderResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/lang")
class LangController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<LangResolver> = map {
        LangResolver(
            id = it[LangEntity.id].value
        )
    }

    fun Query.toFilter(filter: List<String>?): Query = also {
        for (item in filter ?: listOf()) {
            val value: List<String> = item.split("-")

            if (value[1] == "eq") {
                andWhere { LangEntity.id eq value[2] }
            }
        }
    }

    @GetMapping
    @CrossOrigin
    fun getList(
        @RequestParam filter: List<String>?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
        response: HttpServletResponse,
    ): List<LangResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.LANG,
            method = MethodType.GET,
            group = account.groups,
        )

        val count: Int = LangEntity
            .selectAll()
            .count()

        response.addIntHeader("Content-Size", count)
        response.addHeader("Access-Control-Expose-Headers", "Content-Size")

        LangEntity
            .slice(LangEntity.id)
            .selectAll()
            .toFilter(filter)
            .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: PropertyInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): LangResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.LANG,
            method = MethodType.POST,
            group = account.groups,
        )

        val id: EntityID<String> = try {
            LangEntity.insertAndGetId {
                it[id] = EntityID(input.id ?: throw StaffException("Lang id expected"), LangEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong lang")
        }

        LangEntity
            .select { LangEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("Wrong lang")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: PropertyInput,
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): LangResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.LANG,
            method = MethodType.PUT,
            group = account.groups,
        )

        try {
            LangEntity.update(
                where = { LangEntity.id eq id }
            ) {
                it[this.id] = EntityID(input.id, LangEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong property")
        }

        LangEntity
            .select { LangEntity.id eq input.id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("Property not found")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): LangResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.LANG,
            method = MethodType.DELETE,
            group = account.groups,
        )

        LangEntity
            .select { LangEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?.also { LangEntity.deleteWhere { LangEntity.id eq id } }
            ?: throw NoDataException("Wrong language")
    }
}