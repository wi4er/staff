package com.example.staff.resolver

import com.example.staff.model.ContactType

data class ContactResolver(
    val id: String,
    val type: ContactType,
)
