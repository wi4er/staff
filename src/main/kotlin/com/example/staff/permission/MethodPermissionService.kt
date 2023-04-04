package com.example.staff.permission

import com.example.staff.exception.PermissionException
import com.example.staff.model.MethodPermissionEntity
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Component

@Component
class MethodPermissionService {
    fun check(
        entity: EntityType,
        method: MethodType,
        group: List<Int>,
    ) {
        MethodPermissionEntity.select {
            MethodPermissionEntity.method eq method and (MethodPermissionEntity.entity eq entity) and (
                MethodPermissionEntity.group inList group
            )
        }.firstOrNull() ?: throw PermissionException("Permission denied!")
    }
}