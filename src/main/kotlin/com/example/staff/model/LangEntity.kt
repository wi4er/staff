package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object LangEntity: IdTable<String>(name = "lang") {
    override val id: Column<EntityID<String>> = varchar("id", length = 50)
        .uniqueIndex()
        .primaryKey()
        .entityId()
}