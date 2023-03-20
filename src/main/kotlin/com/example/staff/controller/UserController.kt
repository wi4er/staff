package com.example.staff.controller

import com.example.staff.input.UserInput
import com.example.staff.model.User2GroupEntity
import com.example.staff.model.UserEntity
import com.example.staff.resolver.UserResolver
import com.example.staff.saver.user.UserSaver
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/user")
class UserController(
    val saver: List<UserSaver>,
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

    @GetMapping
    fun getList(): List<UserResolver> = transaction {
        UserEntity
            .join(User2GroupEntity, JoinType.LEFT)
            .selectAll()
            .toResolver()
    }

    @PostMapping
    fun addItem(
        @RequestBody input: UserInput,
    ): UserResolver = transaction {
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
    }

    @PutMapping
    fun updateItem(
        @RequestBody input: UserInput,
    ) = transaction {
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
    }

    @DeleteMapping
    fun deleteItem(
        @RequestParam
        id: Int
    ): UserResolver = transaction {
        UserEntity
            .join(User2GroupEntity, JoinType.LEFT)
            .select { UserEntity.id eq id }
            .toResolver()
            .firstOrNull()
            ?.also { UserEntity.deleteWhere { UserEntity.id eq id } }
            ?: throw Exception("Wrong user")
    }
}