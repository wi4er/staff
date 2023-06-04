package com.example.staff.filler.user

import com.example.staff.model.User2ContactEntity
import com.example.staff.resolver.UserContactResolver
import com.example.staff.resolver.UserResolver
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Service

@Service
class UserContactFiller: UserFiller {
    override fun fill(map: MutableMap<Int, UserResolver>) {
        User2ContactEntity
            .select { User2ContactEntity.user inList map.keys }
            .forEach { contact ->
                map[contact[User2ContactEntity.user].value]?.contact?.add(
                    UserContactResolver(
                        contact = contact[User2ContactEntity.contact].value,
                        value = contact[User2ContactEntity.value],
                    )
                )
            }
    }
}