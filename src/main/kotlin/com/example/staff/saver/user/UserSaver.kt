package com.example.staff.saver.user

import com.example.staff.input.UserInput
import org.jetbrains.exposed.dao.EntityID

interface UserSaver {
    fun save(userId: EntityID<Int>, input: UserInput)
}