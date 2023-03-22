package com.example.staff.model

import org.jetbrains.exposed.dao.IntIdTable


enum class EntityType {
    USER,
    GROUP,
}

enum class MethodType {
    GET,
    POST,
    PUT,
    DELETE,
}

object MethodPermissionEntity: IntIdTable(name = "permission_method") {
    val method = enumeration(name = "method", klass = MethodType::class)

    val entity = enumeration(name = "entity", klass = EntityType::class)

    val group = reference(name = "group", GroupEntity)
}