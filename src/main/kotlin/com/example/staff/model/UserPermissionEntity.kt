package com.example.staff.model

import com.example.staff.permission.MethodType
import org.jetbrains.exposed.dao.IntIdTable

object UserPermissionEntity: IntIdTable(name = "user_permission") {
    val user = reference("user", UserEntity)
    val group = reference("group", GroupEntity)
    val method = enumeration(name = "method", klass = MethodType::class)
}