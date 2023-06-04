package com.example.staff.saver.user

import com.example.staff.input.UserInput
import com.example.staff.input.UserPropertyInput
import com.example.staff.model.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Component

@Component
class UserStringSaver : UserSaver {
    private fun Set<UserPropertyInput>.delete(userId: EntityID<Int>) {
        forEach {
            User2StringEntity.deleteWhere {
                User2StringEntity.property eq it.property and (
                User2StringEntity.value eq (it.value ?: "")
                ) and (
                User2StringEntity.user eq userId
                )
            }
        }
    }

    private fun Set<UserPropertyInput>.insert(userId: EntityID<Int>) {
        forEach { input ->
            User2StringEntity.insert {
                it[property] = EntityID(input.property, ContactEntity)
                it[value] = input.value ?: ""
                input.lang?.let { some -> it[lang] = EntityID(some, LangEntity) }
                it[user] = userId
            }
        }
    }

    private fun getStrings(): Set<String> {
        return PropertyEntity
            .select {
                PropertyEntity.type eq PropertyType.STRING
            }
            .map { it[PropertyEntity.id].value }
            .toSet()
    }

    override fun save(userId: EntityID<Int>, input: UserInput) {
        val current = User2StringEntity
            .select { User2StringEntity.user eq userId }
            .map {
                UserPropertyInput().also { new ->
                    new.property = it[User2StringEntity.property].value
                    new.value = it[User2StringEntity.value]
                    new.lang = it[User2StringEntity.lang]?.value
                }
            }.toSet()

        val stringProps: Set<String> = getStrings()

        val update: Set<UserPropertyInput> = input.property
            .filter { stringProps.contains(it.property) }
            .toSet()

        (current - update).delete(userId)
        (update - current).insert(userId)
    }
}