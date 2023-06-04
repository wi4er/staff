package com.example.staff.saver.user

import com.example.staff.exception.StaffException
import com.example.staff.input.UserInput
import com.example.staff.input.UserPropertyInput
import com.example.staff.model.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.springframework.stereotype.Service

@Service
class UserUserSaver: UserSaver {
    private fun Set<UserPropertyInput>.delete(userId: EntityID<Int>) {
        forEach {
            User2UserEntity.deleteWhere {
                User2UserEntity.property eq it.property and  (
                User2UserEntity.user eq (it.value?.toIntOrNull() ?: throw StaffException("Wrong user id"))
                ) and (
                User2UserEntity.user eq userId
                )
            }
        }
    }

    private fun Set<UserPropertyInput>.insert(userId: EntityID<Int>) {
        forEach { input ->
            User2UserEntity.insert {
                it[property] = EntityID(input.property, PropertyEntity)
                it[child] = EntityID(input.value?.toIntOrNull() ?: throw StaffException("Wrong user id"), UserEntity)
                it[user] = userId
            }
        }
    }

    private fun getStrings(): Set<String> {
        return PropertyEntity
            .select { PropertyEntity.type eq PropertyType.USER }
            .map { it[PropertyEntity.id].value }
            .toSet()
    }

    override fun save(userId: EntityID<Int>, input: UserInput) {
        val current = User2UserEntity
            .select { User2UserEntity.user eq userId }
            .map {
                UserPropertyInput().also { new ->
                    new.property = it[User2UserEntity.property].value
                    new.value = it[User2UserEntity.id].value.toString()
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