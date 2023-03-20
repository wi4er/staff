package com.example.staff.model

import org.jetbrains.exposed.dao.IntIdTable

object GroupEntity: IntIdTable(name = "user_group") {
    val parent = reference("parentId", GroupEntity).nullable()
}
