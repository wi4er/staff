package com.example.staff.model

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Column

object UserEntity : IntIdTable(name = "user") {
    val name: Column<String> = varchar("name", 100)
}