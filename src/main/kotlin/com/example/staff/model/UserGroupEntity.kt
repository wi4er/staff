package com.example.staff.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object UserGroupEntity: IntIdTable(name = "user_group") {

}

class UserGroup(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<UserGroup>(UserGroupEntity)

    val user by User via User2UserGroup
}