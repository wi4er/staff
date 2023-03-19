package com.example.staff.controller

import com.example.staff.model.UserEntity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.bind.annotation.*

data class UserResolver(
    private val item: ResultRow
) {
    val id = item[UserEntity.id].value
    val login = item[UserEntity.login]
}

class UserInput {
    var id: Int? = null
    var login: String = ""
}

@RestController
@RequestMapping("/user")
class UserController {
    @GetMapping
    fun getList(): List<UserResolver> = transaction {
        UserEntity.selectAll().map(::UserResolver)
    }

    @PostMapping
    fun addItem(
        @RequestBody input: UserInput,
    ): UserResolver = transaction {
        UserEntity.insertAndGetId {
            it[login] = input.login
        }.let {
            UserEntity.select { UserEntity.id eq it }.firstOrNull()
                ?.let(::UserResolver)
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
        }.let {
            UserEntity.select { UserEntity.id eq it }.firstOrNull()
                ?.let(::UserResolver)
        } ?: throw Exception("Wrong user")
    }

    @DeleteMapping
    fun deleteItem(
        @RequestParam
        id: Int
    ): UserResolver = transaction {
        UserEntity.select { UserEntity.id eq id }
            .firstOrNull()
            ?.let(::UserResolver)
            ?.also { UserEntity.deleteWhere { UserEntity.id eq id } }
            ?: throw Exception("Wrong user")
    }
}