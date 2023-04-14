package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object PointEntity: IdTable<String>(name = "directory_point") {
    override val id: Column<EntityID<String>> = varchar("id", length = 50)
        .uniqueIndex()
        .primaryKey()
        .entityId()

    val directory = reference("directory", DirectoryEntity)
}