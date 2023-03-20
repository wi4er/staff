package com.example.staff.model

import org.jetbrains.exposed.sql.Table

object User2GroupEntity: Table(name = "user2group") {
    val user = reference("user", UserEntity)
    val group = reference("group", GroupEntity)
}