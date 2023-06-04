package com.example.staff.model

import org.jetbrains.exposed.dao.IntIdTable

object User2UserEntity: IntIdTable(name = "user2user") {
    val property = reference("property", PropertyEntity)
    val user = reference("user", UserEntity)
    val child = reference("child", UserEntity)
}