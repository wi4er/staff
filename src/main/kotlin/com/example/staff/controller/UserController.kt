package com.example.staff.controller

import com.example.staff.exception.PermissionException
import com.example.staff.input.UserInput
import com.example.staff.model.EntityType
import com.example.staff.model.MethodType
import com.example.staff.model.User2GroupEntity
import com.example.staff.model.UserEntity
import com.example.staff.permission.Account
import com.example.staff.permission.AccountFactory
import com.example.staff.permission.MethodPermissionService
import com.example.staff.resolver.UserResolver
import com.example.staff.saver.user.UserSaver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    val saver: List<UserSaver>,
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<UserResolver> {
        return fold(mutableMapOf<Int, UserResolver>()) { acc, row ->
            acc[row[UserEntity.id].value]?.let {
                it.group.add(row[User2GroupEntity.group].value)
            } ?: run {
                acc[row[UserEntity.id].value] = UserResolver(
                    id = row[UserEntity.id].value,
                    login = row[UserEntity.login],
                    group = row[User2GroupEntity.group]?.value?.let {
                        mutableListOf(it)
                    } ?: mutableListOf(),
                )
            }
            acc
        }.values.toList()
    }

    fun Query.toFilter(filter: Map<String, String>?): Query = also {
        filter?.get("filter[group]")?.let { group ->
            andWhere { User2GroupEntity.group eq group.toInt() }
        }
    }

    @GetMapping
    fun getList(
        @RequestParam filter: Map<String, String>?,
        @RequestParam limit: Int?,
        @RequestParam offset: Int?,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): List<UserResolver> = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.USER,
                method = MethodType.GET,
                group = account.groups,
            )

            UserEntity
                .join(User2GroupEntity, JoinType.LEFT)
                .selectAll()
                .toFilter(filter)
                .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
                .toResolver()
        } ?: throw PermissionException("Permission denied!")
    }

    @PostMapping
    fun addItem(
        @RequestBody input: UserInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): UserResolver = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.USER,
                method = MethodType.POST,
                group = account.groups,
            )

            UserEntity.insertAndGetId {
                it[login] = input.login
            }.also { id ->
                saver.forEach { it.save(id, input) }
            }.let { id ->
                UserEntity
                    .join(User2GroupEntity, JoinType.LEFT)
                    .select { UserEntity.id eq id }
                    .toResolver()
                    .firstOrNull()
            } ?: throw Exception("Wrong user")
        } ?: throw PermissionException("Permission denied!")
    }

    @PutMapping
    fun updateItem(
        @RequestBody input: UserInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ) = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.USER,
                method = MethodType.PUT,
                group = account.groups,
            )

            UserEntity.update(
                where = { UserEntity.id eq input.id }
            ) {
                it[login] = input.login
            }.also { id ->
                saver.forEach { it.save(EntityID(id, UserEntity), input) }
            }.let {
                UserEntity
                    .join(User2GroupEntity, JoinType.LEFT)
                    .select { UserEntity.id eq it }
                    .toResolver()
                    .firstOrNull()
            } ?: throw Exception("Wrong user")
        } ?: throw PermissionException("Permission denied!")
    }

    @DeleteMapping
    fun deleteItem(
        @RequestParam id: Int,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): UserResolver = transaction {
        val account: Account? = authorization?.let(accountFactory::createFromToken)

        account?.let {
            permissionService.check(
                entity = EntityType.USER,
                method = MethodType.DELETE,
                group = account.groups,
            )

            UserEntity
                .join(User2GroupEntity, JoinType.LEFT)
                .select { UserEntity.id eq id }
                .toResolver()
                .firstOrNull()
                ?.also { UserEntity.deleteWhere { UserEntity.id eq id } }
                ?: throw Exception("Wrong user")
        } ?: throw PermissionException("Permission denied!")
    }
}