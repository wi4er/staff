package com.example.staff.input

import com.example.staff.permission.EntityType
import com.example.staff.permission.MethodType

class PermissionInput {
    var id: Int? = null
    var method: MethodType? = null
    var group: Int? = null
    var entity: EntityType? = null
}