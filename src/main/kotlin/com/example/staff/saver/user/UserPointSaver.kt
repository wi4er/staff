package com.example.staff.saver.user

import com.example.staff.input.UserInput
import org.jetbrains.exposed.dao.EntityID
import org.springframework.stereotype.Service

@Service
class UserPointSaver: UserSaver {
    override fun save(userId: EntityID<Int>, input: UserInput) {

    }
}