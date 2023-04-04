package com.example.staff.model

import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType
import org.jetbrains.exposed.dao.IntIdTable

object MethodPermissionEntity: IntIdTable(name = "permission_method") {
    val method = enumeration(name = "method", klass = MethodType::class)
    val entity = enumeration(name = "entity", klass = EntityType::class)
    val group = reference(name = "group", GroupEntity)
}