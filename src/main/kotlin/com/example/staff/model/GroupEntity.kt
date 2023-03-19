package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object GroupEntity: IntIdTable(name = "user_group") {

}

class Group(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Group>(GroupEntity)

    val user by User via User2UserGroup
}