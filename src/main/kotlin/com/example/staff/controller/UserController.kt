package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.input.UserInput
import com.example.staff.model.*
import com.example.staff.permission.*
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
    fun Query.toResolver(): List<UserResolver> = mutableMapOf<Int, UserResolver>().also { map ->
        forEach {
            map[it[UserEntity.id].value] = UserResolver(
                id = it[UserEntity.id].value,
                login = it[UserEntity.login],
                group = mutableListOf(),
            )
        }

        User2GroupEntity
            .select { User2GroupEntity.user inList map.keys }
            .forEach { group ->
                map[group[User2GroupEntity.user].value]?.let {
                    it.group.add(group[User2GroupEntity.group].value)
                }
            }
    }.values.toList()


    fun Query.toFilter(filter: Map<String, String>?): Query = also {
//        filter?.get("filter[group]")?.let { group ->
//            andWhere { User2GroupEntity.group eq group.toInt() }
//        }
    }

    @GetMapping
    @CrossOrigin
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

            addLogger(StdOutSqlLogger)

            UserEntity
//                .join(User2GroupEntity, JoinType.LEFT)
                .join(UserPermissionEntity, JoinType.INNER) {
                    UserEntity.id eq UserPermissionEntity.user and (
                        UserPermissionEntity.group inList account.groups and (
                            UserPermissionEntity.method eq MethodType.GET)
                        )
                }
                .slice(UserEntity.id, UserEntity.login)
                .selectAll()
                .toFilter(filter)
                .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
                .groupBy(UserEntity.id)
                .orderBy(UserEntity.login)
                .toResolver()
        } ?: throw PermissionException("Permission denied!")
    }

    @PostMapping
    @CrossOrigin
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
    @CrossOrigin
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
    @CrossOrigin
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
                .join(UserPermissionEntity, JoinType.INNER) {
                    UserEntity.id eq UserPermissionEntity.user and (
                        UserPermissionEntity.group inList account.groups and (
                            UserPermissionEntity.method eq MethodType.DELETE)
                        )
                }
                .select { UserEntity.id eq id }
                .toResolver()
                .firstOrNull()
                ?.also { UserEntity.deleteWhere { UserEntity.id eq id } }
                ?: throw NoDataException("Wrong user")
        } ?: throw PermissionException("Permission denied!")
    }
}