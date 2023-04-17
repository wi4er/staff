package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IdTable
import org.jetbrains.exposed.sql.Column

object ContactEntity: IdTable<String>(name = "user_contact") {
    override val id: Column<EntityID<String>> = varchar("id", length = 50)
        .uniqueIndex()
        .primaryKey()
        .entityId()

    val type: Column<ContactType> = enumerationByName(name = "type", klass = ContactType::class, length = 50)
}