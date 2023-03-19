package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object UserEntity : IntIdTable(name = "user") {
    val login: Column<String> = varchar("login", 100)
        .uniqueIndex()
}

object User2UserGroup: Table() {
    val user = reference("user", UserEntity)
    val group = reference("group", GroupEntity)
}

class User(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<User>(UserEntity)

    var login by UserEntity.login

    var group by Group via User2UserGroup
}