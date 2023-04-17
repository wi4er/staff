package com.example.staff.controller

import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.ProviderInput
import com.example.staff.model.DirectoryEntity
import com.example.staff.model.PropertyEntity
import com.example.staff.model.ProviderEntity
import com.example.staff.permission.*
import com.example.staff.resolver.DirectoryResolver
import com.example.staff.resolver.PropertyResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/directory")
class DirectoryController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<DirectoryResolver> = map {
        DirectoryResolver(
            id = it[DirectoryEntity.id].value
        )
    }

    @GetMapping
    @CrossOrigin
    fun getList(
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
        response: HttpServletResponse,
    ): List<DirectoryResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.DIRECTORY,
            method = MethodType.GET,
            group = account.groups,
        )

        val count: Int = DirectoryEntity
            .selectAll()
            .count()

        response.addIntHeader("Content-Size", count)
        response.addHeader("Access-Control-Expose-Headers", "Content-Size")

        DirectoryEntity
            .slice(DirectoryEntity.id)
            .selectAll()
            .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: ProviderInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): DirectoryResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.DIRECTORY,
            method = MethodType.POST,
            group = account.groups,
        )

        val id: EntityID<String> = try {
            DirectoryEntity.insertAndGetId {
                it[id] = EntityID(input.id ?: throw StaffException("Directory id expected"), DirectoryEntity)
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong provider")
        }

        DirectoryEntity
            .select { DirectoryEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw PermissionException("Permission denied!")
    }
}