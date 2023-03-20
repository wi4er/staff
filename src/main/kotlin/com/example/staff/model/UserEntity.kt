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
