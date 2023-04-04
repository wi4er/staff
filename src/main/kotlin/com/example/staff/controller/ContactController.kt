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
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.CONTACT,
                method = MethodType.GET,
                group = account.groups,
            )

            ContactEntity
                .selectAll()
                .toResolver()
        } ?: throw PermissionException("Permission denied!")
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: ContactInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): ContactResolver = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.CONTACT,
                method = MethodType.POST,
                group = account.groups,
            )

            val id: EntityID<String> = try {
                ContactEntity.insertAndGetId {
                    it[id] = EntityID(input.id, ContactEntity)
                    input.type?.let { some -> it[type] = ContactType.valueOf(some) }
                }
            } catch (ex: Exception) {
                throw StaffException("Wrong contact")
            }

            ContactEntity
                .select { ContactEntity.id eq id }
                .toResolver()
                .firstOrNull()
        } ?: throw PermissionException("Permission denied!")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: ContactInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ) = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
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
        } ?: throw PermissionException("Permission denied!")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: String,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): ContactResolver = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
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
        } ?: throw PermissionException("Permission denied!")
    }
}