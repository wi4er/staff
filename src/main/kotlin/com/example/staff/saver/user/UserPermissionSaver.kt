package com.example.staff.saver.user

import com.example.staff.exception.StaffException
import com.example.staff.input.UserInput
import com.example.staff.model.GroupEntity
import com.example.staff.model.UserPermissionEntity
import com.example.staff.permission.MethodType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.springframework.stereotype.Component

@Component
class UserPermissionSaver: UserSaver {
    override fun save(userId: EntityID<Int>, input: UserInput) {
        for (item in input.permission) {
            UserPermissionEntity.insert {
                it[user] = userId
                it[group] = EntityID(item.group, GroupEntity)
                it[method] = MethodType.valueOf(item.method ?: throw StaffException("Wrong method!"))
            }
        }
    }
}