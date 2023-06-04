package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object PropertyEntity: IdTable<String>(name = "property") {
    override val id: Column<EntityID<String>> = varchar("id", length = 50)
        .uniqueIndex()
        .primaryKey()
        .entityId()

    val type: Column<PropertyType> = enumerationByName(
        name = "type",
        klass = PropertyType::class,
        length = 10,
    )
}