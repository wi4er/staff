package com.example.staff.resolver

import com.example.staff.model.ContactEntity
import com.example.staff.model.ContactType
import org.jetbrains.exposed.sql.ResultRow

data class ContactResolver(
    val id: String,
    val type: ContactType,
) {
    companion object {
        fun fromResult(row: ResultRow): ContactResolver = ContactResolver(
            id = row[ContactEntity.id].value,
            type = row[ContactEntity.type],
        )
    }
}
