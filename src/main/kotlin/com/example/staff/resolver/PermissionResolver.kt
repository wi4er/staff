package com.example.staff.resolver

import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType

data class PermissionResolver(
    val method: MethodType,
    val entity: EntityType,
    val group: Int,
)
