package com.example.staff.controller

import com.example.staff.exception.NoDataException
import com.example.staff.exception.PermissionException
import com.example.staff.exception.StaffException
import com.example.staff.filler.user.UserFiller
import com.example.staff.input.UserInput
import com.example.staff.model.*
import com.example.staff.permission.*
import com.example.staff.resolver.UserContactResolver
import com.example.staff.resolver.UserPointResolver
import com.example.staff.resolver.UserResolver
import com.example.staff.resolver.UserStringResolver
import com.example.staff.saver.user.UserSaver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/user")
class UserController(
    val saver: List<UserSaver>,
    val filler: List<UserFiller>,
    val accountFactory: AccountFactory,
    val permissionService: MethodPermissionService,
) {
    fun Query.toResolver(): List<UserResolver> = mutableMapOf<Int, UserResolver>().also { map ->
        forEach {
            map[it[UserEntity.id].value] = UserResolver(
                id = it[UserEntity.id].value,
                login = it[UserEntity.login],
            )
        }

        filler.forEach { it.fill(map) }
    }.values.toList()


    fun Query.toFilter(filter: List<String>?): Query = also {
//        filter?.get("filter[group]")?.let { group ->
//            andWhere { User2GroupEntity.group eq group.toInt() }
//        }

        for (item in filter ?: listOf()) {
            val value: List<String> = item.split("-")

            if (value[1] == "eq") {
                andWhere { PropertyEntity.id eq value[2] }
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
    ): List<UserResolver> = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.USER,
            method = MethodType.GET,
            group = account.groups,
        )

        val count: Int = UserEntity
            .join(UserPermissionEntity, JoinType.INNER) {
                UserEntity.id eq UserPermissionEntity.user and (
                UserPermissionEntity.group inList account.groups
                ) and (
                UserPermissionEntity.method eq MethodType.GET
                )
            }
            .selectAll()
            .toFilter(filter)
            .count()

        response.addIntHeader("Content-Size", count)
        response.addHeader("Access-Control-Expose-Headers", "Content-Size")

        UserEntity
            .join(UserPermissionEntity, JoinType.INNER) {
                UserEntity.id eq UserPermissionEntity.user and (
                UserPermissionEntity.group inList account.groups
                ) and (
                UserPermissionEntity.method eq MethodType.GET
                )
            }
            .slice(UserEntity.id, UserEntity.login)
            .selectAll()
            .toFilter(filter)
            .also { it.limit(limit ?: Int.MAX_VALUE, offset ?: 0) }
            .groupBy(UserEntity.id)
            .orderBy(UserEntity.login)
            .toResolver()
    }

    @PostMapping
    @CrossOrigin
    fun addItem(
        @RequestBody input: UserInput,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): UserResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.USER,
            method = MethodType.POST,
            group = account.groups,
        )

        val id: EntityID<Int> = try {
            UserEntity.insertAndGetId { insert ->
                input.id?.let { insert[id] = EntityID(it, UserEntity) }
                insert[login] = input.login ?: throw StaffException("Login expected!")
            }.also { id -> saver.forEach { it.save(id, input) } }
        } catch (ex: Exception) {
            throw StaffException("Wrong user")
        }

        UserEntity
            .select { UserEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("User not found")
    }

    @PutMapping
    @CrossOrigin
    fun updateItem(
        @RequestBody input: UserInput,
        @RequestParam id: Int,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): UserResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

        permissionService.check(
            entity = EntityType.USER,
            method = MethodType.PUT,
            group = account.groups,
        )

        try {
            UserEntity.update(
                where = { UserEntity.id eq id }
            ) {
                it[this.id] = EntityID(id, UserEntity)
                it[login] = input.login ?: throw StaffException("Login expected!")
            }
        } catch (ex: Exception) {
            throw StaffException("Wrong user")
        }.also { id ->
            saver.forEach { it.save(EntityID(id, UserEntity), input) }
        }

        UserEntity
            .select { UserEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?: throw NoDataException("User not found")
    }

    @DeleteMapping
    @CrossOrigin
    fun deleteItem(
        @RequestParam id: Int,
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String?,
    ): UserResolver = transaction {
        authorization ?: throw PermissionException("Permission denied!")

        val account: Account = authorization.let(accountFactory::createFromToken)

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
    }
}