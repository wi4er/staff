package com.example.staff.saver.user

import com.example.staff.input.UserInput
import com.example.staff.model.GroupEntity
import com.example.staff.model.User2GroupEntity
import com.example.staff.model.UserEntity
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Component

@Component
class UserGroupSaver : UserSaver {
    override fun save(userId: EntityID<Int>, input: UserInput) {
        val groups: MutableSet<Int> = User2GroupEntity
            .select { User2GroupEntity.user eq userId }
            .map { it[User2GroupEntity.group].value }
            .toMutableSet()

        input.group.forEach { groupId ->
            if (!groups.contains(groupId)) {
                User2GroupEntity.insert {
                    it[user] = userId
                    it[group] = EntityID(groupId, GroupEntity)
                }
                groups.remove(groupId)
            }
        }

        groups.forEach {
            User2GroupEntity.deleteWhere { User2GroupEntity.group eq it }
        }
    }
}