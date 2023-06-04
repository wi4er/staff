package com.example.staff.saver.user

import com.example.staff.input.UserContactInput
import com.example.staff.input.UserInput
import com.example.staff.model.ContactEntity
import com.example.staff.model.User2ContactEntity
import com.example.staff.model.User2GroupEntity
import com.example.staff.model.UserEntity
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Service

@Service
class UserContactSaver : UserSaver {
    override fun save(userId: EntityID<Int>, input: UserInput) {
        val current: Set<UserContactInput> = User2ContactEntity
            .select { User2ContactEntity.user eq userId }
            .map {
                UserContactInput().also { new ->
                    new.contact = it[User2ContactEntity.contact].value
                    new.value = it[User2ContactEntity.value]
                }
            }.toSet()

        val update = input.contact.toSet()

        (current - update).forEach {
            User2ContactEntity.deleteWhere {
                User2ContactEntity.contact eq it.contact and (
                    User2ContactEntity.value eq (it.value ?: "")
                    ) and (
                    User2ContactEntity.user eq userId
                    )
            }
        }

        (update - current).forEach { input ->
            User2ContactEntity.insert {
                it[contact] = EntityID(input.contact, ContactEntity)
                it[value] = input.value ?: ""
                it[user] = userId
            }
        }
    }
}