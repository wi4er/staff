package com.example.staff.model

import org.jetbrains.exposed.dao.IntIdTable

object User2PointEntity: IntIdTable(name = "user2point") {
    val property = reference("property", PropertyEntity)
    val user = reference("user", UserEntity)
    val point = reference("point", PointEntity)
}