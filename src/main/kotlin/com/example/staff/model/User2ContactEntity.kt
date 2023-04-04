package com.example.staff.model

import org.jetbrains.exposed.dao.IntIdTable

object User2ContactEntity: IntIdTable(name = "user2contact") {
    val user = reference("user", UserEntity)
    val contact = reference("contact", ContactEntity)
    val value = varchar("value", length = 100)
}