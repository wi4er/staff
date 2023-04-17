package com.example.staff.model

import com.example.staff.permission.MethodType
import org.jetbrains.exposed.dao.IntIdTable

object ContactPermissionEntity: IntIdTable(name = "user_contact_permission") {
    val contact = reference("contact", ContactEntity)
    val group = reference("group", GroupEntity)
    val method = enumeration(name = "method", klass = MethodType::class)
}