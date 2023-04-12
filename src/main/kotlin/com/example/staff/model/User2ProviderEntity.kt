package com.example.staff.model

import org.jetbrains.exposed.sql.Table

object User2ProviderEntity: Table(name = "user2provider")  {
    val user = reference("user", UserEntity)
    val provider = reference("provider", ProviderEntity)
    val hash = varchar("hash", length = 256)
}