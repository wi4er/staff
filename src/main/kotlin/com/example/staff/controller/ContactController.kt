package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.input.ContactInput
import com.example.staff.model.*
import com.example.staff.permission.*
import com.example.staff.resolver.ContactResolver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/contact")
class ContactController(
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<ContactResolver> {
        println(this)

        return map {
            ContactResolver(
                id = it[ContactEntity.id].value,
                type = it[ContactEntity.type],
            )
        }
    }

    @GetMapping
    @CrossOrigin
    fun getList(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): List<ContactResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.CONTACT,
            method = MethodType.GET,
            group = account.groups,
        )

        ContactEntity
            .selectAll()
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: ContactInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): ContactResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.CONTACT,
            method = MethodType.POST,
            group = account.groups,
        )

        try {
            val row = ContactEntity.insert {
                it[id] = EntityID(input.id ?: throw StaffException("Wrong permission"), ContactEntity)
                it[type] = ContactType.valueOf(input.type ?: throw StaffException("Wrong permission"))
            }
                .resultedValues
                ?: throw StaffException("Wrong Contact")

            ContactResolver.fromResult(row.first())
        } catch (ex: Exception) {
            throw StaffException("Wrong contact")
        }
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: ContactInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ) = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.CONTACT,
            method = MethodType.PUT,
            group = account.groups,
        )

        ContactEntity
            .join(ContactPermissionEntity, JoinType.INNER) {
                ContactEntity.id eq ContactPermissionEntity.contact and (
                    ContactPermissionEntity.group inList account.groups and (
                        ContactPermissionEntity.method eq MethodType.PUT))
            }
            .select { ContactEntity.id eq input.id }
            .firstOrNull()
            ?: throw PermissionException("Permission denied!")

        ContactEntity.update(
            where = { ContactEntity.id eq input.id }
        ) {
            input.type?.let { some -> it[type] = ContactType.valueOf(some) }
        }.let {
            ContactEntity
                .select { ContactEntity.id eq input.id }
                .toResolver()
                .firstOrNull()
        } ?: throw NoDataException("Wrong contact")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): ContactResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.CONTACT,
            method = MethodType.DELETE,
            group = account.groups,
        )

        ContactEntity
            .join(ContactPermissionEntity, JoinType.INNER) {
                ContactEntity.id eq ContactPermissionEntity.contact and (
                    ContactPermissionEntity.group inList account.groups and (
                        ContactPermissionEntity.method eq MethodType.DELETE))
            }
            .select { ContactEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?.also { ContactEntity.deleteWhere { ContactEntity.id eq id } }
            ?: throw NoDataException("Wrong contact")
    }
}