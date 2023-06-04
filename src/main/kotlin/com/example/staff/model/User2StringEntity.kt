package com.example.staff.model

import org.jetbrains.exposed.dao.IntIdTable

object User2StringEntity: IntIdTable(name = "user2string") {
    val property = reference("property", PropertyEntity)
    val user = reference("user", UserEntity)
    val lang = reference("lang", LangEntity).nullable()
    val value = varchar("value", length = 256)
}