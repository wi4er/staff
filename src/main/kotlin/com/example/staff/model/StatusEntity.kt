package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object StatusEntity: IdTable<String>(name = "status") {
    override val id: Column<EntityID<String>> = ProviderEntity.varchar("id", length = 50)
        .uniqueIndex()
        .primaryKey()
        .entityId()
}