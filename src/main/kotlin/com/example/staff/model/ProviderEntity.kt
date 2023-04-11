package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object ProviderEntity: IdTable<String>(name = "provider") {
    override val id: Column<EntityID<String>> = ContactEntity.varchar("id", length = 50)
        .uniqueIndex()
        .primaryKey()
        .entityId()

}